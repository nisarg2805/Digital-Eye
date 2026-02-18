package com.example.myapplication.Utilities;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechManager {
    private static final String TAG = "TextToSpeechManager";
    public TextToSpeech textToSpeech;
    public boolean is_init = false;

    public TextToSpeechManager(Activity activity) {
        textToSpeech = new TextToSpeech(activity.getApplicationContext(), new OnInitListener() {
            @Override
            public void onInit(int status) {
                // onInit Runs on UI Thread so it Slows the App
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // if No error is found then only it will run
                        if (status == TextToSpeech.SUCCESS) {
                            // To Choose language of speech
                            textToSpeech.setLanguage(Locale.UK);
                            textToSpeech.setSpeechRate((float) 0.8);
                            textToSpeech.setPitch((float) 1.2);
                            Log.d(TAG, "Starting Service for " + activity.toString());
                            is_init = true;
                        } else {
                            is_init = false;
                            Log.d(TAG, "Service Error Code" + status + "for" + activity.toString());
                        }
                    }
                }).start();
            }
        });

    }

    public boolean isInitialized() {
        return is_init;
    }

    public void speakOut(String text, int queue_options) throws InterruptedException {
        textToSpeech.speak(text, queue_options, null);
        while (textToSpeech.isSpeaking()) {
        }
        Thread.sleep(100);
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.speak("",TextToSpeech.QUEUE_FLUSH,null);
            textToSpeech.stop();
        }
    }

    public void finalize() {
        Log.d(TAG, "Shutting Down");
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}
