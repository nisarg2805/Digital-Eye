package com.example.myapplication.Activities;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.myapplication.R;
import com.example.myapplication.Utilities.TextToSpeechManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class ReadBookActivity extends Activity {
    final String TAG = "ReadBookActivity";
    public List bookAssetArray = new ArrayList();

    TextToSpeechManager textToSpeechManager;
    TextView bookBody, bookName;
    //TouchManager
    TextView touchCount;
    volatile int tapCount = 0;
    //Threads
    final Handler ui_handler = new Handler();
    Thread menuThread;

    final Runnable touch_runnable = new Runnable() {
        BufferedReader reader;

        public void readBook(String file_name) {
            try {
                final InputStream file = getAssets().open("ebooks/" + file_name);
                reader = new BufferedReader(new InputStreamReader(file));
                String line = reader.readLine();
                while (line != null) {
                    line = reader.readLine();
                    textToSpeechManager.speakOut(line, TextToSpeech.QUEUE_ADD);
                    if (tapCount == 2) {
                        menuThread.interrupt();
                        ui_handler.post(() -> {
                            menuThread.interrupt();
                            menuThread = null;
                            ReadBookActivity.this.finish();
                        });
                        break;
                    } else if (tapCount > 2) {
                        runOnUiThread(() -> {
                            tapCount = 0;
                            touchCount.setText(String.valueOf(tapCount));
                        });
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void loadDisplay(String file_name) {
            try {
                String title = file_name.replace('-', ' ').toUpperCase(Locale.ROOT).replace(".TXT", "");
                runOnUiThread(() -> bookName.setText(title));
                final InputStream file = getAssets().open("ebooks/" + file_name);
                reader = new BufferedReader(new InputStreamReader(file));
                String line = reader.readLine();
                String finalLine = "";
                while (line != null) {
                    line = reader.readLine();
                    finalLine += line + "\n";
                }
                String finalLine1 = finalLine;
                ui_handler.post(() -> {
                    bookBody.setText(finalLine1);

                });
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

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

                        textToSpeechManager.speakOut("Reading E Book on random", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap twice anytime to stop reading and to go to Main Page", TextToSpeech.QUEUE_FLUSH);
                        int randomNum = ThreadLocalRandom.current().nextInt(0, 7 + 1);

                        Thread.sleep(2000);

                        // reset for going back
                        tapCount = 0;
                        runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));
                        String booktmp = (String) bookAssetArray.get(randomNum);
                        Log.d(TAG,"Reading "+booktmp+randomNum);
                        loadDisplay(booktmp);
                        readBook(booktmp);
                        textToSpeechManager.speakOut("",TextToSpeech.QUEUE_FLUSH);
                        Log.d(TAG, "Activity Stop" + LocalDateTime.now());
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };

    public boolean listAssetFiles(String path) {
        String[] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file))
                        return false;
                    else {
                        bookAssetArray.add(file);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readbook);
        touchCount = findViewById(R.id.touchCount);
        //GET EBOOK FILE
        listAssetFiles("ebooks");
        Log.d(TAG, bookAssetArray.toString());
        //Start other Activity
        textToSpeechManager = new TextToSpeechManager(this);

        bookBody = findViewById(R.id.bookBody);
        bookName = findViewById(R.id.bookName);

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

        textToSpeechManager.stop();
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
