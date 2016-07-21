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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class BattleActivity extends AppCompatActivity{
    private final int BATTLE_FRAME=30;
    private ProgressBar tensionBar;
    private boolean onBattle;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private ImageView imageView5;
    private TextView textView;
    private Line line;
    private RelativeLayout layout;
    private FrameLayout layout2;

    private TimerTask battle;
    private TimerTask stuck;
    private Handler handler = new Handler();

    private int waterLevel;

    private double maxStrength;
    private double strength;
    private double weight;
    private double distance;
    private double length;
    private double tension;
    private double maxHp;
    private double hp;
    private double durability;

    private int state; //LOOSING = 0, HOLDING = 1, REELING = 2
    private final int LOOSING=0;
    private final int REELING=1;

    private double reelW; //angular velocity
    private double pivotX;
    private double pivotY;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        tensionBar = (ProgressBar) findViewById(R.id.progressBar_battle_tension);
        tensionBar.setVisibility(View.INVISIBLE);
        onBattle = false;

        imageView1 = (ImageView) findViewById(R.id.imageView_battle_rod_normal);
        imageView2 = (ImageView) findViewById(R.id.imageView_battle_rod_bent);
        imageView3 = (ImageView) findViewById(R.id.imageView_battle_water);
        imageView4 = (ImageView) findViewById(R.id.imageView_battle_land);
        imageView5 = (ImageView) findViewById(R.id.imageView_battle_reel);
        waterLevel=imageView3.getTop();

        textView=(TextView) findViewById(R.id.textView_battle_debug);

        layout = (RelativeLayout) findViewById(R.id.layout_battle);
        layout2 = (FrameLayout) findViewById(R.id.framelayout_battle);

        Button button = (Button) findViewById(R.id.button_battle_back);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });

        imageView3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(onBattle)
                    return true;
                if(event.getX()>=0 && event.getX()<imageView4.getLeft()-imageView3.getLeft() &&
                        event.getY()>=0 && event.getY()<=imageView3.getBottom()-imageView3.getTop()){
                    if (line != null) {
                        line.toX = (int) event.getX() + imageView3.getLeft();
                        line.toY = (int) event.getY() + imageView3.getTop();
                        line.cutY(waterLevel);
                        line.invalidate();
                    } else {
                        waterLevel = imageView3.getTop();
                        line = new Line(getApplicationContext(), Color.BLACK, imageView1.getLeft() + layout2.getLeft(),
                                imageView1.getTop() + layout2.getTop(), (int) event.getX() + imageView3.getLeft(), (int) event.getY() + waterLevel);
                        line.cutY(waterLevel);
                        layout.addView(line);
                    }
                }

                if(event.getAction()==MotionEvent.ACTION_UP) {
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
                    textView.setText("");
                    timer.scheduleAtFixedRate(stuck, 0, 100);
                }
                return true;
            }
        });

        imageView5.setOnTouchListener(new View.OnTouchListener() {
            double prevAngle;
            double curAngle;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                curAngle = Math.atan2(event.getY()-pivotY, event.getX()-pivotX);
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    state=REELING;
                    prevAngle=curAngle;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    state=LOOSING;
                }

                reelW+=curAngle-prevAngle;
                if(prevAngle>Math.PI*2/3 && curAngle < -Math.PI*2/3){
                    reelW+=2*Math.PI;
                }
                if(prevAngle < -Math.PI*2/3 && curAngle > Math.PI*2/3){
                    reelW-=2*Math.PI;
                }
                prevAngle=curAngle;
                return true;
            }
        });
    }


    private void beginBattle() {
        if(onBattle){
            return;
        }
        pivotX = (double)(imageView5.getWidth())/2;
        pivotY = (double)(imageView5.getHeight())/2;
        onBattle = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                tensionBar.setVisibility(View.VISIBLE);
                imageView1.setVisibility(View.INVISIBLE);
                imageView2.setVisibility(View.VISIBLE);
                tensionBar.setMax(600);
                tensionBar.setProgress(0);

                Display dis = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                dis.getMetrics(metrics);
                line.fromX = imageView2.getLeft()+layout2.getLeft();
                line.fromY = imageView2.getTop()+layout2.getTop()+6*metrics.densityDpi/160;
                line.cutY(waterLevel);
                line.invalidate();
            }
        });

        distance=1020;
        length=1000;
        maxStrength=150;
        strength=maxStrength;
        weight=100;
        tension=200;
        durability=100;

        maxHp=1000;
        hp=maxHp;
        reelW=0;
        Timer timer = new Timer();
        battle = new TimerTask() {
            double ratio = 1-Math.pow(0.9, 5.0/BATTLE_FRAME);
            double ratio2 = 1-Math.pow(0.75, 5.0/BATTLE_FRAME);
            public void run() {
                length-=reelW*5;
                strength = (maxStrength*hp)/maxHp+weight;
                if(state==REELING){
                    tension = ((distance-length)*25)*0.25 + tension*0.75;
                }
                else if(state==LOOSING){
                    tension-=tension*ratio2 + 30*ratio2;
                    length-=(tension-strength)*ratio;
                    /*if(strength>tension) {
                        distance += (strength - tension)*(4.0)/BATTLE_FRAME;
                    }*/
                }
                distance-=(tension-strength)*ratio;
                if(distance<length){
                    distance=length;
                }
                if(distance<50) endBattle(true);

                if(tension>450) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tensionBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        }
                    });
                    durability -= (tension - 450)*(1.0/BATTLE_FRAME);
                    if(durability<0){
                        endBattle(false);
                        return;
                    }
                }
                else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tensionBar.getProgressDrawable().clearColorFilter();
                        }
                    });
                }
                reelW=0;

                if(tension>600 || tension<strength/4) {
                    endBattle(false);
                }
                if(tension>=300)
                    hp-=(tension-300)*(2.0/BATTLE_FRAME);
                if(hp<0)    hp=0;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("distance: "+(int)distance);
                        textView.append("\nlength: "+(int)length);
                        textView.append("\ntension: "+(int)tension);
                        textView.append("\nfish strength: "+(int)strength);
                        textView.append("\nfish hp: "+(int)hp);
                        tensionBar.setProgress((int)tension);
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
                imageView1.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.INVISIBLE);
                line.setVisibility(View.INVISIBLE);
                layout.removeView(line);
                line = null;
            }
        });
        state=LOOSING;
        battle.cancel();
    }
}
