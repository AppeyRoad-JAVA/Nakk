/*
 * Copyright (c) 2016  AppeyRoad-JAVA team
 *
 * This file is part of Nakk.
 *
 * Nakk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nakk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nakk.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.appeyroad.nakk;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class BattleActivity extends AppCompatActivity {
    private BattleView battleView;
    private ImageView reelImage;
    private TextView debugText;
    private ImageView tensionBar;
    private ImageView emptyTensionBar;

    public RodModel rodModel;
    public WaterModel waterModel;
    public LandModel landModel;
    public LineModel lineModel;
    private boolean initialized = false;

    private boolean onBattle;
    private int state; //LOOSING = 0, REELING = 1
    private final int LOOSING=0;
    private final int REELING=1;

    private TimerTask battle;
    private TimerTask stuck;
    private Handler handler=new Handler();

    private double pivotX;
    private double pivotY;

    private final int BATTLE_FRAME=30;

    private double reelW; //angular velocity
    private double maxStrength;
    private double strength;
    private double weight;
    private double distance;
    private double length;
    private double maxTension;
    private double tension;
    private double maxHp;
    private double hp;
    private double durability;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        battleView = (BattleView) findViewById(R.id.surfaceview_battle);
        reelImage = (ImageView) findViewById(R.id.imageView_battle_reel);
        tensionBar = (ImageView) findViewById(R.id.imageView_battle_tension_full);
        emptyTensionBar = (ImageView) findViewById(R.id.imageView_battle_tension);
        debugText = (TextView) findViewById(R.id.textView_battle_debug);

        ViewTreeObserver vto = reelImage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                reelImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                pivotX = (double)(reelImage.getWidth())/2;
                pivotY = (double)(reelImage.getHeight())/2;
            }
        });
        tensionBar.setVisibility(View.INVISIBLE);
        emptyTensionBar.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        emptyTensionBar.setPivotY(emptyTensionBar.getTop());
        emptyTensionBar.setVisibility(View.INVISIBLE);
        onBattle = false;

        battleView.setOnTouchListener(new View.OnTouchListener() {
            float prevX;
            float prevY;
            float curX;
            float curY;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(!initialized){
                    rodModel = (RodModel)battleView.renderer.models.get(battleView.renderer.rodIndex.get(0));
                    waterModel = (WaterModel)battleView.renderer.models.get(battleView.renderer.waterIndex.get(0));
                    landModel = (LandModel)battleView.renderer.models.get(battleView.renderer.landIndex.get(0));
                    lineModel = (LineModel)battleView.renderer.models.get(battleView.renderer.lineIndex.get(0));
                    lineModel.setLineStart(rodModel.getRodEnd());
                    initialized=true;
                }
                if (onBattle)
                    return true;
                curX=event.getX();  curY=event.getY();
                float[] position = {curX, battleView.getHeight()-curY};
                float[] waterPos = battleView.renderer.convertPos(position, waterModel.waterLevel); //waterModel에 사영된 좌표
                float[] landPos = battleView.renderer.convertPos(position, landModel.landLevel);    //landModel에 사영된 좌표
                if(!(landPos[0]>=landModel.left && landPos[0]<=landModel.right && landPos[1]>=landModel.back && landPos[1]<=landModel.front) &&
                        (waterPos[0]>=waterModel.left && waterPos[0]<=waterModel.right && waterPos[1]>=waterModel.back && waterPos[1]<=waterModel.front)){
                    lineModel.setFloatPos(waterPos);
                }
                battleView.requestRender();

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Timer timer = new Timer();
                    if (stuck != null) {
                        stuck.cancel();
                    }
                    stuck = new TimerTask() {
                        public void run() {
                            double random = Math.random();
                            if (random <= 0.04) {
                                this.cancel();
                                beginBattle();
                            }
                        }
                    };
                    debugText.setText("");
                    timer.scheduleAtFixedRate(stuck, 0, 100);
                }else if(event.getAction()==MotionEvent.ACTION_MOVE) {
                    float dx = (curX - prevX) / battleView.getWidth() * 180.0f;
                    //float dy = (curY - prevY) / battleView.getHeight() * 180.0f;
                    battleView.renderer.setAngle(battleView.renderer.getAngle() + dx);
                    battleView.requestRender();
                }
                prevX = curX;
                prevY = curY;
                return true;
            }
        });
        reelImage.setOnTouchListener(new View.OnTouchListener() {
            double prevAngle;
            double curAngle;
            float prevX;
            float prevY;
            float curX;
            float curY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                curX = event.getX();
                curY = event.getY();
                curAngle = Math.atan2(curY - pivotY, curX - pivotX);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    state = REELING;
                    prevAngle = curAngle;
                    prevX = curX;
                    prevY = curY;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    state = LOOSING;
                }

                int direction = -1;
                if (curAngle > prevAngle || (curAngle < -Math.PI * 2 / 3 && prevAngle > Math.PI * 2 / 3)) {
                    direction = 1;  //clockwise
                }

                double curR = Math.sqrt((curX - pivotX) * (curX - pivotX) + (curY - pivotY) * (curY - pivotY));
                if (curR >= pivotX * 0.5 && curR <= pivotX * 1.5) {
                    reelW += (Math.sqrt((prevX - curX) * (prevX - curX) + (prevY - curY) * (prevY - curY)) / pivotX) * direction;
                }
                prevAngle = curAngle;
                prevX = curX;
                prevY = curY;
                return true;
            }
        });
    }

    private void beginBattle() {
        if(onBattle){
            return;
        }
        onBattle = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                tensionBar.setVisibility(View.VISIBLE);
                emptyTensionBar.setVisibility(View.VISIBLE);
                emptyTensionBar.setScaleY(0);
            }
        });
        distance=1020;
        length=1000;
        maxStrength=150;
        strength=maxStrength;
        weight=100;
        maxTension=600;
        tension=300;
        durability=100;

        maxHp=1000;
        hp=maxHp;
        reelW=0;

        Timer timer = new Timer();
        battle = new TimerTask() {
            double ratio = 1-Math.pow(0.9, 4.0/BATTLE_FRAME);
            double ratio2 = 1-Math.pow(0.75, 5.0/BATTLE_FRAME);
            public void run() {
                length-=reelW*10;
                strength = (maxStrength*hp)/maxHp+weight;
                if(state==REELING){
                    tension = ((distance-length)*10)*0.25 + tension*0.75;
                }
                else if(state==LOOSING){
                    tension-=tension*ratio2 + 20*ratio2;
                    length-=(tension-strength)*ratio;
                }

                float[] tensionVector = new float[3];
                for(int i=0; i<3; i++){
                    tensionVector[i] = (float)(tension/30)*lineModel.getLineDirection()[i];
                }
                rodModel.setTension(tensionVector);
                battleView.requestRender();

                distance-=(tension-strength)*ratio;
                if(distance<length){
                    distance=length;
                }
                if(distance<50) {
                    endBattle(true);
                    return;
                }

                if(tension>450) {
                    durability -= (tension - 450)*(1.0/BATTLE_FRAME);
                    if(durability<0){
                        endBattle(false);
                        return;
                    }
                }
                reelW=0;

                if(tension>600 || tension<strength/4) {
                    endBattle(false);
                    return;
                }
                if(tension>=300)
                    hp-=(tension-300)*(2.0/BATTLE_FRAME);
                if(hp<0)    hp=0;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        debugText.setText("");
                        debugText.append("distance: "+(int)distance);
                        debugText.append("\nlength: "+(int)length);
                        debugText.append("\ntension: "+(int)tension);
                        debugText.append("\nfish strength: "+(int)strength);
                        debugText.append("\nfish hp: "+(int)hp);
                        emptyTensionBar.setScaleY(1-(float)(tension/maxTension));
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(battle, 0, 1000/BATTLE_FRAME);
    }
    private void endBattle(final boolean win){
        if(!onBattle){
            return;
        }
        onBattle = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                tensionBar.setVisibility(View.INVISIBLE);
                emptyTensionBar.setVisibility(View.INVISIBLE);
                if(win){
                    debugText.append("\nwin!");
                }
                else{
                    debugText.append("\nlose!");
                }
            }
        });
        rodModel.setTension(new float[3]);
        lineModel.initFloatPos();
        battleView.requestRender();
        state=LOOSING;
        battle.cancel();
    }
}