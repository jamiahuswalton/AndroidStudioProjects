package com.anshulagrawal.demoapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    public void clickFunction(View view) {
        Log.i("Info", "Button pressed!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}