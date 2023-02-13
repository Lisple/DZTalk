package com.dztalk.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.databinding.ActivityContactBinding;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.GetUsers;
import com.dztalk.utilities.LocalPreference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ContactActivity extends AppCompatActivity {
    private ActivityContactBinding binding;
    private LocalPreference localPreference;
    private Users selectUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        localPreference = new LocalPreference(getApplicationContext());
        loadSelectedDetails();
        setListener();
    }

    private void setListener(){
        selectUser = (Users) getIntent().getSerializableExtra(UserProperties.KEY_USER);
        System.out.println(selectUser.firstname);
        binding.imageBack.setOnClickListener(v-> onBackPressed());
        binding.talkButton.setOnClickListener(v ->{
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(UserProperties.KEY_USER, selectUser);
            startActivity(intent);
        });
        binding.delete.setOnClickListener(view -> {
            deleteFriend();
            onBackPressed();
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
        binding.ipInfo.setText(selectUser.location);
        binding.hometownInfo.setText(selectUser.hometown);
        binding.birthdayInfo.setText(selectUser.birthday);
    }

    private void deleteFriend(){
        HashMap<String, String> contact = new HashMap<>();
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> senderRequest = new HashMap<>();
        HashMap<String, String> senderContact = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        selectUser = (Users) getIntent().getSerializableExtra(UserProperties.KEY_USER);
        DocumentReference sender =
                db.collection(UserProperties.USER_DB_NAME).document(
                        localPreference.getString(UserProperties.KEY_USER_ID)
                );
        DocumentReference receiver =
                db.collection(UserProperties.USER_DB_NAME).document(
                        selectUser.id
                );
        if(GetUsers.readFromSP(this,UserProperties.KEY_CONTACT)!=null){
            contact = GetUsers.readFromSP(this,UserProperties.KEY_CONTACT);
        }
        if(GetUsers.readFromSP(this,UserProperties.KEY_REQUEST)!=null){
            request = GetUsers.readFromSP(this,UserProperties.KEY_REQUEST);
        }

        if(selectUser.contacts!=null){
            senderContact = selectUser.contacts;
        }
        if(selectUser.request!=null){
            senderRequest = selectUser.request;
        }

        contact.remove(selectUser.id);
        GetUsers.insertToSP(this,UserProperties.KEY_CONTACT,contact);
        request.remove(selectUser.id);
        GetUsers.insertToSP(this,UserProperties.KEY_REQUEST,request);

        senderContact.remove(localPreference.getString(UserProperties.KEY_USER_ID));
        selectUser.contacts = senderContact;
        senderRequest.remove(localPreference.getString(UserProperties.KEY_USER_ID));
        selectUser.request = senderRequest;
        sender.update(UserProperties.KEY_CONTACT,contact)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
        sender.update(UserProperties.KEY_REQUEST,request)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));

        receiver.update(UserProperties.KEY_CONTACT,senderContact)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
        receiver.update(UserProperties.KEY_REQUEST,senderRequest)
                .addOnSuccessListener(unused -> System.out.println("Success"))
                .addOnFailureListener(e -> System.out.println("Fail"));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        overridePendingTransition(0,0);
        finish();
    }

}