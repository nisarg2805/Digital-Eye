package com.example.myapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.myapplication.R;
import com.example.myapplication.Utilities.LoginRegisterManager;
import com.example.myapplication.Utilities.TextToSpeechManager;

import java.time.LocalDateTime;

public class MainPageActivity extends Activity {
    final String TAG = "MainPageActivity";

    String username;
    TextToSpeechManager textToSpeechManager;
    //TouchManager
    TextView touchCount;
    volatile int tapCount = 0;
    //Threads
    final Handler ui_handler = new Handler();
    Thread menuThread;

    final Runnable touch_runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();
            while (thisThread == menuThread && !Thread.currentThread().isInterrupted()) {
                try {
                    if (textToSpeechManager.isInitialized()) {
                        Log.d(TAG, "Activity Start" + LocalDateTime.now());
                        tapCount = 0;
                        runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));

                        textToSpeechManager.speakOut("Welcome, " + username, TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap Once for Shopping", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap Twice for Reading E-Book", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap 3 times to exit app", TextToSpeech.QUEUE_FLUSH);

                        Thread.sleep(3000);

                        switch (tapCount) {
                            case 1: {
                                Log.d(TAG, "Shopping Page");
                                menuThread.interrupt();
                                ui_handler.post(() -> {
                                    menuThread.interrupt();
                                    menuThread = null;
                                    Intent shoppingIntent = new Intent(MainPageActivity.this,
                                            ShoppingActivity.class);
                                    startActivity(shoppingIntent);
                                });
                                break;
                            }
                            case 2: {
                                Log.d(TAG, "Read E-Boook Page");
                                menuThread.interrupt();
                                ui_handler.post(() -> {
                                    menuThread.interrupt();
                                    menuThread = null;
                                    textToSpeechManager.stop();
                                    Intent readBookIntent = new Intent(MainPageActivity.this,
                                            ReadBookActivity.class);
                                    startActivity(readBookIntent);
                                });
                                break;
                            }
                            case 3: {
                                MainPageActivity.this.finishAffinity();
                            }
                            default: {
                                textToSpeechManager.speakOut("Incorrect Choice Try Again", TextToSpeech.QUEUE_FLUSH);
                                tapCount = 0;
                                runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));
                                Thread.sleep(2000);
                            }
                        }

                        Log.d(TAG, "Activity Stop" + LocalDateTime.now());
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        touchCount = findViewById(R.id.touchCount);

        username = new LoginRegisterManager(this).getUserData("name");

        textToSpeechManager = new TextToSpeechManager(this);
        menuThread = new Thread(touch_runnable);
        menuThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int event_action = event.getAction();
        if (event_action == MotionEvent.ACTION_UP) {
            tapCount++;
            if (touchCount != null) {
                touchCount.setText(String.valueOf(tapCount));
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume: " + menuThread);
        super.onResume();

        if (menuThread == null || !menuThread.isAlive()) {
            Log.d(TAG, "OnResume: MenuThreadStart");
            menuThread = new Thread(touch_runnable);
            menuThread.start();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "OnPause:" + menuThread);
        if (menuThread != null && menuThread.isAlive()) {
            Log.d(TAG, "OnPause: MenuThreadStop");
            menuThread.interrupt();
            menuThread = null;
        }
        if (textToSpeechManager != null) {
            textToSpeechManager.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy:");
        if (menuThread != null && menuThread.isAlive()) {
            Log.d(TAG, "OnDestroy: MenuThreadStop");
            menuThread.interrupt();
            menuThread = null;
        }
        super.onDestroy();
    }


}
