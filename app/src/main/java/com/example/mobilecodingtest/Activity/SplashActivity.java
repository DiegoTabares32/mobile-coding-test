package com.example.mobilecodingtest.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.Window;

import com.example.mobilecodingtest.R;

/**
 * Created by Diego on 07/05/2016.
 */
public class SplashActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * Show app logo for 1 second and then go to @MapActivity
          */
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, MapActivity.class);

                startActivity(i);

                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
