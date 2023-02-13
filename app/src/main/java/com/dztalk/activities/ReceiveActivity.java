package com.dztalk.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.databinding.ActivityReceiveBinding;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.GetUsers;
import com.dztalk.utilities.LocalPreference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ReceiveActivity extends AppCompatActivity {
    private ActivityReceiveBinding binding;
    private Users selectUser;
    private LocalPreference localPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        localPreference = new LocalPreference(getApplicationContext());
        loadSelectedDetails();
        setListener();
    }

    private void setListener(){
        binding.imageBack.setOnClickListener(v->onBackPressed());
        binding.accept.setOnClickListener(v->{
            acceptRequest();
            Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(intent);
            finish();
        });
        binding.reject.setOnClickListener(v->{
            rejectRequest();
            Toast.makeText(getApplicationContext(), "Rejected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void loadSelectedDetails() {
        selectUser = (Users) getIntent().getSerializableExtra(UserProperties.KEY_USER);
        byte[] bytes = Base64.decode(selectUser.image,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.profileImage.setImageBitmap(bitmap);
        binding.userName.setText(selectUser.firstname+" "+selectUser.lastname);
        binding.idInfo.setText(selectUser.id);
        binding.emailInfo.setText(selectUser.email);
        binding.desDetail.setText(selectUser.descriptions);
    }

    private void acceptRequest(){
        HashMap<String, String> contact = new HashMap<>();
        HashMap<String, String> senderContact = new HashMap<>();
        HashMap<String, String> request = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        selectUser = (Users) getIntent().getSerializableExtra(UserProperties.KEY_USER);
        DocumentReference sender =
                db.collection(UserProperties.USER_DB_NAME).document(
                        selectUser.id
                );
        DocumentReference receiver =
                db.collection(UserProperties.USER_DB_NAME).document(
                        localPreference.getString(UserProperties.KEY_USER_ID)
                );

        if(selectUser.contacts!=null){
            contact = selectUser.contacts;
        }
        if(GetUsers.readFromSP(this,UserProperties.KEY_CONTACT)!=null){
            senderContact = GetUsers.readFromSP(this,UserProperties.KEY_CONTACT);
        }
        if(GetUsers.readFromSP(this,UserProperties.KEY_REQUEST)!=null){
            request = GetUsers.readFromSP(this,UserProperties.KEY_REQUEST);
        }
        contact.put(localPreference.getString(UserProperties.KEY_USER_ID), "accepted");
        selectUser.contacts = contact;
        senderContact.put(selectUser.id,"accepted");
        GetUsers.insertToSP(this,UserProperties.KEY_CONTACT,senderContact);
        request.put(selectUser.id,"accepted");
        GetUsers.insertToSP(this,UserProperties.KEY_REQUEST,request);
        sender.update(UserProperties.KEY_CONTACT,contact)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
        receiver.update(UserProperties.KEY_CONTACT,senderContact)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
        receiver.update(UserProperties.KEY_REQUEST,request)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
    }

    private void rejectRequest(){
        HashMap<String, String> contact = new HashMap<>();
        HashMap<String, String> request = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        selectUser = (Users) getIntent().getSerializableExtra(UserProperties.KEY_USER);
        DocumentReference sender =
                db.collection(UserProperties.USER_DB_NAME).document(
                        selectUser.id
                );
        DocumentReference receiver =
                db.collection(UserProperties.USER_DB_NAME).document(
                        localPreference.getString(UserProperties.KEY_USER_ID)
                );
        if(selectUser.contacts!=null){
            contact = selectUser.contacts;
        }
        if(GetUsers.readFromSP(this,UserProperties.KEY_REQUEST)!=null){
            request = GetUsers.readFromSP(this,UserProperties.KEY_REQUEST);
        }
        contact.put(localPreference.getString(UserProperties.KEY_USER_ID), "rejected");
        selectUser.contacts = contact;
        request.put(selectUser.id,"rejected");
        GetUsers.insertToSP(this,UserProperties.KEY_REQUEST,request);
        sender.update(UserProperties.KEY_CONTACT,contact)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
        receiver.update(UserProperties.KEY_REQUEST,request)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
}