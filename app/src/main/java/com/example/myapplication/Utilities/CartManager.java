package com.example.myapplication.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

public class CartManager {
    private static final String TAG = "CartManager";
    private SharedPreferences preferences;
    private FirebaseFirestore db;

    public CartManager(Activity activity) {
        db = FirebaseFirestore.getInstance();
        preferences =  activity.getApplicationContext().getSharedPreferences("BlindAppSharedPref", Context.MODE_PRIVATE);
        Log.d(TAG, "CartManager INITIALIZED");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateCart(List cart_items, float cart_total){
        HashMap cartData = new HashMap();
        cartData.put("items", cart_items);
        cartData.put("cartTotal", cart_total);
        cartData.put("time",java.time.LocalDateTime.now());
        cartData.put("userName",preferences.getString("name", ""));
        CollectionReference cart = db.collection("cart");
        cart.add(cartData);
        Log.d(TAG, "Updated Cart");
    }

}
