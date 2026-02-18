package com.example.myapplication.Activities;
import android.app.Activity;
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
import com.example.myapplication.Utilities.CartManager;
import com.example.myapplication.Utilities.TextToSpeechManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ShoppingActivity extends Activity {
    final String TAG = "ShoppingActivity";

    HashMap listfood = new HashMap();
    HashMap listdrinks = new HashMap();

    CartManager cartManager;
    TextToSpeechManager textToSpeechManager;
    //TouchManager
    TextView touchCount;
    int tapCount = 0;
    //Threads
    Thread menuThread;
    Handler ui_handler = new Handler();

    final Runnable touch_runnable = new Runnable() {
        List cart = new ArrayList();
        int cartTotal = 0;
        Object[] array;
        List<Integer> costarray;

        Thread thisThread = Thread.currentThread();

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void itemmenu(HashMap items) throws InterruptedException {
            tapCount = 0;
            runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));

            array = items.keySet().toArray();
            costarray = (List<Integer>) items.values().stream().collect(Collectors.toCollection(ArrayList::new));

            textToSpeechManager.speakOut("Select your preferred items", TextToSpeech.QUEUE_FLUSH);
            for (int i = 0; i < array.length; i++) {
                textToSpeechManager.speakOut("For " + array[i].toString() + "Tap " + (i + 1) + "Time", TextToSpeech.QUEUE_FLUSH);
            }
            textToSpeechManager.speakOut("To go back to Main Shopping Page, tap 4 times", TextToSpeech.QUEUE_FLUSH);

            Thread.sleep(2000);

            if (tapCount > 0 && tapCount < 4) {
                cart.add(array[tapCount - 1]);
                cartTotal = cartTotal + (int) (costarray.get(tapCount - 1));
                textToSpeechManager.speakOut("Added " + array[tapCount - 1] + "to Cart", TextToSpeech.QUEUE_FLUSH);
                textToSpeechManager.speakOut("Your Cart contains", TextToSpeech.QUEUE_FLUSH);
                for (Object e : cart) {
                    Thread.sleep(500);
                    textToSpeechManager.speakOut(e.toString(), TextToSpeech.QUEUE_FLUSH);
                }
                Thread.sleep(1000);
                textToSpeechManager.speakOut("Your cart total is" + cartTotal + " Rupees", TextToSpeech.QUEUE_FLUSH);
            } else {
                if (tapCount != 4)
                    textToSpeechManager.speakOut("Incorrect Choice try again", TextToSpeech.QUEUE_FLUSH);
            }
            Thread.sleep(1000);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            thisThread = Thread.currentThread();
            while (thisThread == menuThread && !Thread.currentThread().isInterrupted()) {
                try {
                    if (textToSpeechManager.isInitialized()) {
                        Log.d(TAG, "Activity Start" + LocalDateTime.now());
                        tapCount = 0;
                        runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));

                        textToSpeechManager.speakOut("Welcome to Shopping Center", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap once for food", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap twice for drinks", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap 3 times to view cart", TextToSpeech.QUEUE_FLUSH);
                        textToSpeechManager.speakOut("Tap 4 times to go Home Page", TextToSpeech.QUEUE_FLUSH);

                        Thread.sleep(3000);

                        switch (tapCount) {
                            case 1: {
                                Log.d(TAG, "Food Page");
                                itemmenu(listfood);
                                break;
                            }
                            case 2: {
                                Log.d(TAG, "Drinks Page");
                                itemmenu(listdrinks);
                                break;
                            }
                            case 3: {
                                tapCount = 0;
                                runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));
                                if (cart.size() > 0) {
                                    Log.d(TAG, "View Cart");
                                    textToSpeechManager.speakOut("Cart contains ", TextToSpeech.QUEUE_FLUSH);
                                    for (Object e : cart) {
                                        Thread.sleep(200);
                                        textToSpeechManager.speakOut(e.toString(), TextToSpeech.QUEUE_FLUSH);
                                    }
                                    textToSpeechManager.speakOut("Your cart total is" + cartTotal + " Rupees", TextToSpeech.QUEUE_FLUSH);
                                    Thread.sleep(200);
                                    textToSpeechManager.speakOut("Tap once to Checkout", TextToSpeech.QUEUE_FLUSH);
                                    Thread.sleep(1500);
                                    if (tapCount == 1) {
                                        cartManager.updateCart(cart, cartTotal);
                                        textToSpeechManager.speakOut("Order Successful", TextToSpeech.QUEUE_FLUSH);
                                        cartTotal = 0;
                                        cart = new ArrayList();
                                    }
                                } else {
                                    Thread.sleep(1000);
                                    textToSpeechManager.speakOut("Cart is Empty", TextToSpeech.QUEUE_FLUSH);
                                }
                                break;
                            }
                            case 4: {
                                menuThread.interrupt();
                                ui_handler.post(() -> {
                                    menuThread.interrupt();
                                    menuThread = null;
                                    ShoppingActivity.this.finish();
                                });
                            }
                            default: {
                                textToSpeechManager.speakOut("Incorrect Choice Try Again", TextToSpeech.QUEUE_FLUSH);
                                tapCount = 0;
                                runOnUiThread(() -> touchCount.setText(String.valueOf(tapCount)));

                            }
                        }
                        Thread.sleep(1000);
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
        setContentView(R.layout.activity_shopping);
        touchCount = findViewById(R.id.touchCount);

        listfood.put("Burger", 60);
        listfood.put("Pizza", 100);
        listfood.put("Salad", 50);

        listdrinks.put("Ice Tea", 30);
        listdrinks.put("Cold Coffee", 40);
        listdrinks.put("Latte", 80);

        textToSpeechManager = new TextToSpeechManager(this);
        cartManager = new CartManager(this);

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
