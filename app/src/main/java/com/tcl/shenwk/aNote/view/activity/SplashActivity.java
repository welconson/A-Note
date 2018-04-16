package com.tcl.shenwk.aNote.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.manager.LoginManager;
import com.tcl.shenwk.aNote.service.ANoteService;

/**
 * Splash screen displaying activity. Here we will check whether there
 * is a user having logged in before. If logged before, we will directly
 * enter HomepageActivity, or enter LoginActivity for user to check in.
 * Created by shenwk on 2018/4/3.
 */

public class SplashActivity extends Activity {
    private static String TAG = "SplashActivity";
    private static int SPLASH_DISPLAY_TIME = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginManager loginManager = LoginManager.getInstance(getApplicationContext());
                if(loginManager.hasLoggedIn(getApplicationContext())) {
                    Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                    startService(new Intent(getApplicationContext(), ANoteService.class));
                    SplashActivity.this.startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    SplashActivity.this.startActivity(intent);
                }
                finish();
            }
        }, SPLASH_DISPLAY_TIME);
    }
}
