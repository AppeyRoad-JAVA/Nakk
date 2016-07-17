package com.appeyroad.nakk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yskim_000 on 2016-07-16.
 */
public class BattleActivity extends Activity{
    public ProgressBar gaugeBar;
    public boolean onBattle;
    public Button button3;
    public Button button4;
    RelativeLayout layout;

    Timer timer;
    TimerTask pull;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_battle);

        gaugeBar = (ProgressBar) findViewById(R.id.progressBar);
        gaugeBar.setVisibility(View.INVISIBLE);
        onBattle = false;

        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginBattle();
            }
        });
        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });
        layout = (RelativeLayout) findViewById(R.id.layout);
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
    public void beginBattle() {
        onBattle = true;
        gaugeBar.setMax(1000);
        gaugeBar.setProgress(200);
        gaugeBar.setVisibility(View.VISIBLE);
        button3.setEnabled(false);

        timer = new Timer();
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
    public void endBattle(boolean win){
        onBattle = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                gaugeBar.setVisibility(View.INVISIBLE);
                button3.setEnabled(true);
            }
        });
        pull.cancel();
    }
}
