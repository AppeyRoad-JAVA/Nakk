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

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class BattleActivity extends AppCompatActivity{

    private ProgressBar gaugeBar;
    private boolean onBattle;
    private Button button;

    private TimerTask pull;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        gaugeBar = (ProgressBar) findViewById(R.id.progressBar_battle_gauge);
        gaugeBar.setVisibility(View.INVISIBLE);
        onBattle = false;

        button = (Button) findViewById(R.id.button_battle_begin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginBattle();
            }
        });
        Button button1 = (Button) findViewById(R.id.button_battle_back);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_battle);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onBattle){
                    gaugeBar.setProgress(gaugeBar.getProgress() + 100);
                    if(gaugeBar.getProgress()>=gaugeBar.getMax()){
                        endBattle(true);
                    }
                }
            }
        });
    }
    private void beginBattle() {
        onBattle = true;
        gaugeBar.setMax(1000);
        gaugeBar.setProgress(200);
        gaugeBar.setVisibility(View.VISIBLE);
        button.setEnabled(false);

        Timer timer = new Timer();
        pull = new TimerTask() {
            public void run() {
                gaugeBar.setProgress(gaugeBar.getProgress() - 40);
                if (gaugeBar.getProgress() <= 0) {
                    endBattle(false);
                }
            }
        };
        timer.scheduleAtFixedRate(pull, 0, 500);
    }
    private void endBattle(boolean win){
        onBattle = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                gaugeBar.setVisibility(View.INVISIBLE);
                button.setEnabled(true);
            }
        });
        pull.cancel();
    }
}
