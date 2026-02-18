package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.myapplication.Activities.LoginActivity;
import com.example.myapplication.Activities.MainPageActivity;
import com.example.myapplication.Activities.RegisterActivity;
import com.example.myapplication.Utilities.LoginRegisterManager;
import com.example.myapplication.Utilities.TextToSpeechManager;
import java.time.LocalDateTime;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    final Handler ui_handler = new Handler();
    final Runnable touch_runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();
            while (thisThread == menuThread && !Thread.currentThread().isInterrupted()) {
                try {

                    if (textToSpeechManager.isInitialized()) {

                        Log.d(TAG,"Activity Start" + LocalDateTime.now());
                        tapCount = 0;

                        runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));
                        Thread.sleep(1000);
                        textToSpeechManager.speakOut("Are you registered?", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap Once for Yes", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap Twice for No", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap 3 times to exit app", TextToSpeech.QUEUE_FLUSH);

                        Thread.sleep(3000);

                        switch (tapCount) {
                            case 1: {
                                Log.d(TAG,"Login Page");
                                menuThread.interrupt();
                                ui_handler.post(() -> {
                                    menuThread.interrupt();
                                    menuThread=null;
                                    textToSpeechManager.stop();
                                    Intent loginIntent = new Intent(MainActivity.this,
                                            LoginActivity.class);
                                    startActivity(loginIntent);
                                });
                                break;
                            }
                            case 2: {
                                Log.d(TAG,"Registration Page");
                                menuThread.interrupt();
                                ui_handler.post(() -> {
                                    menuThread.interrupt();
                                    menuThread=null;
                                    textToSpeechManager.stop();
                                    Intent registerIntent = new Intent(MainActivity.this,
                                            RegisterActivity.class);
                                    startActivity(registerIntent);
                                });
                                break;
                            }
                            case 3: {
                                MainActivity.this.finishAffinity();
                            }
                            default: {
                                textToSpeechManager.speakOut("Incorrect Choice Try Again", TextToSpeech.QUEUE_FLUSH);
                                tapCount = 0;
                                runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));
                                Thread.sleep(3000);
                            }
                        }
                        Log.d(TAG,"Activity Stop" + LocalDateTime.now());
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };

    Thread menuThread;
    int tapCount = 0;
    TextToSpeechManager  textToSpeechManager;
    TextView touchCount;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int event_action = event.getAction();
        if (event_action == MotionEvent.ACTION_UP) {
            tapCount ++;
            if(touchCount!=null){
                touchCount.setText(String.valueOf(tapCount));
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginRegisterManager loginRegisterManager = new LoginRegisterManager(this);
        if(loginRegisterManager.checkPreviousLogin()){
            Log.d(TAG,"LOADING MAIN PAGE");
            Intent intentMainPage = new Intent(this, MainPageActivity.class);
            startActivity(intentMainPage);
            return;
        }
        touchCount = findViewById(R.id.touchCount);
        textToSpeechManager = new TextToSpeechManager(this);
        menuThread = new Thread(touch_runnable);
        menuThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginRegisterManager loginRegisterManager = new LoginRegisterManager(this);
        if(loginRegisterManager.checkPreviousLogin()){
            Log.d(TAG,"LOADING MAIN PAGE");
            if(textToSpeechManager!=null){
                textToSpeechManager.stop();
            }
            Intent intentMainPage = new Intent(this, MainPageActivity.class);
            startActivity(intentMainPage);
            return;
        }

        Log.d(TAG,"OnResume: "+menuThread);
        if( menuThread==null || !menuThread.isAlive()){
            Log.d(TAG,"OnResume: MenuThreadStart");
            menuThread = new Thread(touch_runnable);
            menuThread.start();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"OnPause:"+menuThread);
        if(menuThread!=null && menuThread.isAlive()){
            Log.d(TAG,"OnPause: MenuThreadStop");
            menuThread.interrupt();
            menuThread = null;
        }
        if(textToSpeechManager!=null){
            textToSpeechManager.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy:");
        if(menuThread!=null && menuThread.isAlive()){
            Log.d(TAG,"OnDestroy: MenuThreadStop");
            menuThread.interrupt();
            menuThread = null;
        }
        super.onDestroy();
    }

}