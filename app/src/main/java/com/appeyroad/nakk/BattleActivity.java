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
    private ProgressBar tensionBar;
    private boolean onBattle;
    //private Button button;
    private Button button2;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
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
    private double tension;
    private double maxHp;
    private double hp;
    private boolean reeling;
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
        waterLevel=imageView3.getTop();

        textView=(TextView) findViewById(R.id.textView_battle_debug);

        layout = (RelativeLayout) findViewById(R.id.layout_battle);
        layout2 = (FrameLayout) findViewById(R.id.framelayout_battle);
        /*button = (Button) findViewById(R.id.button_battle_begin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginBattle();
            }
        });*/

        Button button1 = (Button) findViewById(R.id.button_battle_back);
        button1.setOnClickListener(new View.OnClickListener(){
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
                if(line!=null){
                    line.toX = (int)event.getX()+imageView3.getLeft();
                    line.toY = (int)event.getY()+imageView3.getTop();
                    line.cutY(waterLevel);
                    line.invalidate();
                }
                else {
                    waterLevel=imageView3.getTop();
                    line = new Line(getApplicationContext(), Color.BLACK, imageView1.getLeft() + layout2.getLeft(),
                            imageView1.getTop() + layout2.getTop(), (int) event.getX() + imageView3.getLeft(), (int) event.getY() + waterLevel);
                    line.cutY(waterLevel);
                    layout.addView(line);
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
                    timer.scheduleAtFixedRate(stuck, 0, 100);
                }
                return true;
            }
        });
        button2 = (Button) findViewById(R.id.button_battle_reel);
        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    reeling=true;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    reeling=false;
                }
                return false;
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
                //button.setEnabled(false);
                button2.setEnabled(true);
                imageView1.setVisibility(View.INVISIBLE);
                imageView2.setVisibility(View.VISIBLE);
                tensionBar.setMax(400);
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

        distance=1050;
        maxStrength=150;
        strength=maxStrength;
        weight=100;
        tension=200;

        maxHp=1000;
        hp=maxHp;

        Timer timer = new Timer();
        battle = new TimerTask() {
            public void run() {
                distance-=(tension-strength)/10;
                if(distance<50) endBattle(true);

                strength = (maxStrength*hp)/maxHp+weight;

                if(reeling) {
                    tension += Math.abs(tension - 200) / 20 + 10;
                }
                else {
                    if(tension>strength){
                        tension-=Math.abs(tension - strength)/4+20;
                    }
                    else {
                        tension -= 5;
                    }
                }

                if(tension>400 || tension<strength/4)
                    endBattle(false);
                if(tension>=200)
                    hp-=(tension-200)/5;
                if(hp<0)    hp=0;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("distance: "+(int)distance);
                        textView.append("\ntension: "+(int)tension);
                        textView.append("\nfish strength: "+(int)strength);
                        textView.append("\nfish hp: "+(int)hp);
                        tensionBar.setProgress((int)tension);
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(battle, 0, 100);
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
                //button.setEnabled(true);
                button2.setEnabled(false);
                imageView1.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.INVISIBLE);
                line.setVisibility(View.INVISIBLE);
                layout.removeView(line);
                line = null;
            }
        });
        reeling=false;
        battle.cancel();
    }
}
