package com.example.myapplication.Activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myapplication.R;
import com.example.myapplication.Utilities.LoginRegisterManager;
import com.example.myapplication.Utilities.TextToSpeechManager;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";

    // UI Variables
    TextInputEditText name_EditText;
    //    TextInputEditText email_EditText;
    TextInputEditText password_EditText;
    TextInputEditText contact_EditText;
    TextInputEditText birthPlace_EditText;
    TextInputEditText firstSchool_EditText;
    AppCompatButton registerBtn;

    //Touch Handling Variables
    TextView touchCount;
    int tapCount = 0;
    // SpeechRecognizer Variables
    final int REQ_CODE_SPEECH_INPUT = 100;
    String speechToTextResult;
    boolean speakCompleted = false;
    // TextToSpeech Variables
    TextToSpeechManager textToSpeechManager;
    //TapMenu Thread & UI Handler
    Thread menuThread;
    Handler ui_handler = new Handler();

    final Runnable touch_runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public boolean getSpeechTextInput(TextInputEditText editTextInput, String initialPrompt) throws InterruptedException {
            if (editTextInput.getText().toString().equals("")) {

                textToSpeechManager.speakOut("For Registration please", TextToSpeech.QUEUE_FLUSH);
                textToSpeechManager.speakOut(initialPrompt, TextToSpeech.QUEUE_FLUSH);

                String tmp = startVoiceInput();
                if (tmp == null) {
                    textToSpeechManager.speakOut("There was error recognizing speech", TextToSpeech.QUEUE_FLUSH);
                    return false;
                } else {
                    tapCount = 0;
                    runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));

                    textToSpeechManager.speakOut(tmp + ", Tap Once to Confirm", TextToSpeech.QUEUE_FLUSH);
                    textToSpeechManager.speakOut("Tap Twice to try Again", TextToSpeech.QUEUE_FLUSH);
                    textToSpeechManager.speakOut("Tap 3 Times to Go Back", TextToSpeech.QUEUE_FLUSH);

                    Thread.sleep(3000);

                    if (editTextInput == contact_EditText) {
                        if (TextUtils.isEmpty(tmp) || !Patterns.PHONE.matcher(tmp).matches()) {
                            textToSpeechManager.speakOut("Error enter valid phone", TextToSpeech.QUEUE_FLUSH);
                            textToSpeechManager.speakOut("Try Again", TextToSpeech.QUEUE_FLUSH);
                            runOnUiThread(() -> contact_EditText.setText(""));
                            return false;
                        }
                    }
                    switch (tapCount) {
                        case 1: {
                            runOnUiThread(() -> editTextInput.setText(tmp));
                            break;
                        }
                        case 2: {
                            return false;
                        }
                        case 3: {
                            menuThread.interrupt();
                            ui_handler.post(() -> {
                                menuThread.interrupt();
                                menuThread = null;
                                RegisterActivity.this.finish();
                            });
                            break;
                        }
                        default: {
                            textToSpeechManager.speakOut("Incorrect Choice Try Again", TextToSpeech.QUEUE_FLUSH);

                            tapCount = 0;
                            runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));
                            return false;
                        }
                    }
                }
                Thread.sleep(2000);
            }
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();
            while (thisThread == menuThread && !Thread.currentThread().isInterrupted()) {
                try {

                    if (textToSpeechManager.isInitialized()) {
                        Log.d(TAG, "Menu Activity Start" + LocalDateTime.now());

                        tapCount = 0;
                        runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));
                        //Thread CoolDown
                        Thread.sleep(1000);

                        if (!getSpeechTextInput(name_EditText, "Speak your Name")) {
                            Thread.sleep(1000);
                            continue;
                        }

                        if (!getSpeechTextInput(password_EditText, "Speak password")) {
                            Thread.sleep(1000);
                            continue;
                        }
                        if (!getSpeechTextInput(contact_EditText, "Speak Contact Number")) {
                            Thread.sleep(1000);
                            continue;
                        }
                        if (!getSpeechTextInput(birthPlace_EditText, "Speak your Birth Place")) {
                            Thread.sleep(1000);
                            continue;
                        }
                        if (!getSpeechTextInput(firstSchool_EditText, "Speak your First School")) {
                            Thread.sleep(1000);
                            continue;
                        }

                        if (!name_EditText.getText().toString().equals("") &&
                                !password_EditText.getText().toString().equals("") &&
                                !contact_EditText.getText().toString().equals("") &&
                                !birthPlace_EditText.getText().toString().equals("") &&
                                !firstSchool_EditText.getText().toString().equals("")) {
                            LoginRegisterManager registerManager = new LoginRegisterManager(RegisterActivity.this);
                            registerManager.addUser(name_EditText.getText().toString(),
                                    password_EditText.getText().toString(),
                                    contact_EditText.getText().toString(),
                                    birthPlace_EditText.getText().toString(),
                                    firstSchool_EditText.getText().toString());

                            textToSpeechManager.speakOut("You are successfully Registered", TextToSpeech.QUEUE_FLUSH);
                            Log.d(TAG, "Registration Complete");
                            menuThread.interrupt();
                            ui_handler.post(() -> {
                                menuThread.interrupt();
                                menuThread = null;
                                RegisterActivity.this.finish();
                            });
                        }

                        Log.d(TAG, "Activity Stop" + LocalDateTime.now());
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textToSpeechManager = new TextToSpeechManager(this);

        touchCount = findViewById(R.id.touchCount);
        name_EditText = findViewById(R.id.editTextPersonName);
        password_EditText = findViewById(R.id.editTextPassword);
        contact_EditText = findViewById(R.id.editTextPhone);
        birthPlace_EditText = findViewById(R.id.editTextBirthPlace);
        firstSchool_EditText = findViewById(R.id.editTextFirstSchool);
        registerBtn = findViewById(R.id.appCompatButtonRegister);

        registerBtn.setOnClickListener(this::onRegisterBtn);

        menuThread = new Thread(touch_runnable);
        menuThread.start();
    }

    public void onRegisterBtn(View view) {
        if (!name_EditText.getText().toString().equals("") &&
                !password_EditText.getText().toString().equals("") &&
                !contact_EditText.getText().toString().equals("") &&
                !birthPlace_EditText.getText().toString().equals("") &&
                !firstSchool_EditText.getText().toString().equals("")) {
            LoginRegisterManager registerManager = new LoginRegisterManager(RegisterActivity.this);
            registerManager.addUser(name_EditText.getText().toString(),
                    password_EditText.getText().toString(),
                    contact_EditText.getText().toString(),
                    birthPlace_EditText.getText().toString(),
                    firstSchool_EditText.getText().toString());

            try {
                textToSpeechManager.speakOut("You are successfully Registered", TextToSpeech.QUEUE_FLUSH);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Registration Page");
            menuThread.interrupt();
            ui_handler.post(() -> {
                menuThread.interrupt();
                menuThread = null;
                RegisterActivity.this.finish();
            });
        }
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    public String startVoiceInput() {
        speakCompleted = false;
        speechToTextResult = null;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, "2000");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, "3000");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Log.d(TAG, "startVoiceInput: ERROR WITH STARTING Speech INTENT");
            return null;
        }
        long time = System.currentTimeMillis();
        //Return Result
        while (!speakCompleted || speechToTextResult == null) {
            if ((System.currentTimeMillis() - time) > 15000) {
                speakCompleted = true;
                Log.d(TAG, "startVoiceInput: No SpeechInput Past 15 seconds");
                return null;
            }
        }
        speakCompleted = true;
        return speechToTextResult;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                speechToTextResult = result.get(0);
                speakCompleted = true;

                runOnUiThread(() -> {
                    speechToTextResult = result.get(0);
                    speakCompleted = true;
                });

//                Log.d(TAG, "SpeechToText Result: "+speechToTextResult);
            } else {
                Log.d(TAG, "Speech Recognition error");
            }
            Log.d(TAG, java.time.LocalDateTime.now().toString());
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume: " + menuThread);
        super.onResume();
        if (speakCompleted) {
            if (menuThread == null || !menuThread.isAlive()) {
                Log.d(TAG, "OnResume: MenuThreadStart");
                menuThread = new Thread(touch_runnable);
                menuThread.start();
            }
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "OnPause:" + menuThread);
        if (speakCompleted) {
            if (menuThread != null && menuThread.isAlive()) {
                Log.d(TAG, "OnPause: MenuThreadStop");
                menuThread.interrupt();
                menuThread = null;
            }
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
