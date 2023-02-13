package com.dztalk.utilities;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

public class GetUsers {
    public static Users setUser(QueryDocumentSnapshot queryDocumentSnapshot){
        Users user = new Users();
        user.firstname =  queryDocumentSnapshot.getString(UserProperties.KEY_FIRSTNAME);
        user.lastname =  queryDocumentSnapshot.getString(UserProperties.KEY_LASTNAME);
        user.fullname = user.firstname+ " "+ user.lastname;
        user.email =  queryDocumentSnapshot.getString(UserProperties.KEY_EMAIL);
        user.gender = queryDocumentSnapshot.getString(UserProperties.KEY_GENDER);
        user.image = queryDocumentSnapshot.getString(UserProperties.KEY_USER_Profile);
        user.id = queryDocumentSnapshot.getId();
        user.contacts = (HashMap<String, String>) queryDocumentSnapshot.get(UserProperties.KEY_CONTACT);
        user.request = (HashMap<String, String>) queryDocumentSnapshot.get(UserProperties.KEY_REQUEST);
        user.location = queryDocumentSnapshot.getString(UserProperties.KEY_LOCATION);
        user.hometown = queryDocumentSnapshot.getString(UserProperties.KEY_HOMETOWN);
        user.birthday = queryDocumentSnapshot.getString(UserProperties.KEY_BIRTHDAY);
        user.status = queryDocumentSnapshot.getString(UserProperties.KEY_STATUS);
        user.job = queryDocumentSnapshot.getString(UserProperties.KEY_JOB);
        user.descriptions = queryDocumentSnapshot.getString(UserProperties.KEY_DESCRIPTION);
        return user;
    }

    public static HashMap<String, String> readFromSP(Activity activity, String key){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("HashMap", MODE_PRIVATE);
        String defValue = new Gson().toJson(new HashMap<String, String>());
        String json=sharedPreferences.getString(key,defValue);
        TypeToken<HashMap<String,String>> token = new TypeToken<HashMap<String,String>>() {};
        HashMap<String,String> retrievedMap=new Gson().fromJson(json,token.getType());
        return retrievedMap;
    }

    public static void insertToSP(Activity activity, String key, HashMap<String, String> jsonMap) {
        String jsonString = new Gson().toJson(jsonMap);
        System.out.println("JsonString "+jsonString);
        SharedPreferences sharedPreferences = activity.getSharedPreferences("HashMap", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, jsonString);
        editor.apply();
    }
}
