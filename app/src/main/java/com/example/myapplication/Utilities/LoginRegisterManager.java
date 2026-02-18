package com.example.myapplication.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginRegisterManager {
    private static final String TAG = "LoginRegisterManager";
    private SharedPreferences preferences;
    private FirebaseFirestore db;

    public LoginRegisterManager(Activity activity) {
        db = FirebaseFirestore.getInstance();
        preferences =  activity.getApplicationContext().getSharedPreferences("BlindAppSharedPref", Context.MODE_PRIVATE);
    }

    public void addUser(String name, String password, String contact, String birthPlace, String firstSchool) {
        CollectionReference user = db.collection("User");

        HashMap<String,String> newuser = new HashMap<String, String>() {{
            put("name",name);
            put("password",password);
            put("contact",contact);
            put("birthPlace",birthPlace);
            put("firstSchool",firstSchool);
        }};
        user.add(newuser).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // after the data addition is successful
                SharedPreferences.Editor myEdit = preferences.edit();
                myEdit.putString("name",name);
                myEdit.putString("password",password);
                myEdit.putString("contact",contact);
                myEdit.putString("birthPlace",birthPlace);
                myEdit.putString("firstSchool",firstSchool);
                Log.d(TAG, "User Added");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // this method is called when the data addition process is failed.
                Log.d(TAG, "User Add Error");
            }
        });
    }

    public boolean checkUser(String name, String password) {

        CollectionReference user = db.collection("User");
        user.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(name.equalsIgnoreCase((String) document.getData().get("name")) && password.equalsIgnoreCase((String) document.getData().get("password"))){
                                        Log.d(TAG, "Got Matching Users "+document.getId() + " => " + document.getData());
                                        SharedPreferences.Editor myEdit = preferences.edit();
                                        myEdit.putString("name",name);
                                        myEdit.putString("password",password);
                                        myEdit.commit();
                                    }
                                }
                            } else {
                                SharedPreferences.Editor myEdit = preferences.edit();
                                myEdit.clear();
                                myEdit.commit();
                                Log.d(TAG, "Error getting Users: ", task.getException());
                            }
                        }
                    });
        return !preferences.getString("name", "").isEmpty();
    }

    public boolean checkPreviousLogin() {
        Log.d(TAG, "Preference Found" + preferences.getAll());
        return !preferences.getString("name", "").isEmpty();
    }

    public String getUserData(String item) {
        Log.d(TAG, "Preference Found" + preferences.getAll());
        return preferences.getString(item, "");
    }
}
