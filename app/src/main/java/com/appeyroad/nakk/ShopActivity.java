package com.appeyroad.nakk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by 남기원 on 2016-07-22.
 */
public class ShopActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
    }

    public void onShopBackClicked(View v) {
        finish();
    }

    public void onNullClicked(View v) {
        Toast.makeText(getApplicationContext(), "아직 구현되지 않았습니다.", Toast.LENGTH_LONG).show();
    }
}
