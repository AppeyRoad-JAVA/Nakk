package com.appeyroad.nakk;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;

/**
 * Created by yskim_000 on 2016-07-16.
 */
public class BattleActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_battle);


    }
}
