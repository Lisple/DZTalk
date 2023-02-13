package com.dztalk.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.databinding.ActivityRequestBinding;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.GetUsers;
import com.dztalk.utilities.LocalPreference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RequestActivity extends AppCompatActivity {
    private ActivityRequestBinding binding;
    private Users selectUser;
    private LocalPreference localPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        localPreference = new LocalPreference(getApplicationContext());
        loadSelectedDetails();
        setListener();
    }

    private void setListener(){
        binding.imageBack.setOnClickListener(v-> onBackPressed());
        binding.request.setOnClickListener(v ->{
            sendRequest();
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

    private void sendRequest(){
        HashMap<String, String> contact = new HashMap<>();
        HashMap<String, String> request = new HashMap<>();
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
        if(selectUser.request!=null){
            request = selectUser.request;
        }

        if(contact.containsKey(selectUser.id)&&contact.get(selectUser.id).equals("pending")){
            System.out.println("here");
            Toast.makeText(getApplicationContext(), "You have already added!", Toast.LENGTH_SHORT).show();
        }else{
            contact.put(selectUser.id, "pending");
            GetUsers.insertToSP(this,UserProperties.KEY_CONTACT,contact);
            request.put(localPreference.getString(UserProperties.KEY_USER_ID),"pending");
            selectUser.request = request;
            sender.update(UserProperties.KEY_CONTACT,contact)
                    .addOnSuccessListener(unused -> System.out.println("Success"))
                    .addOnFailureListener(e -> System.out.println("Fail"));
            receiver.update(UserProperties.KEY_REQUEST,request)
                    .addOnSuccessListener(unused -> System.out.println("Success"))
                    .addOnFailureListener(e -> System.out.println("Fail"));
            Toast.makeText(getApplicationContext(), "Send Request", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), SearchActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
}