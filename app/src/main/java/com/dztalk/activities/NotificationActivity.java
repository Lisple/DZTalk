package com.dztalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.adapators.UserAdapter;
import com.dztalk.databinding.ActivityNotificationBinding;
import com.dztalk.listeners.UserListener;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.GetUsers;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements UserListener {
    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadRequests();
        setListener();
    }

    private void setListener(){
        binding.imageBack.setOnClickListener(v->onBackPressed());
    }

    private void loadRequests(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(UserProperties.USER_DB_NAME)
            .get()
            .addOnCompleteListener(task -> {
                loading(false);
                if(task.isSuccessful() && task.getResult() != null){
                    List<Users> users = new ArrayList<>();
                    HashMap<String, String> request = GetUsers.readFromSP(this,UserProperties.KEY_REQUEST);
                    for(String s: request.keySet()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if(queryDocumentSnapshot.getId().equals(s)){
                                Users user = GetUsers.setUser(queryDocumentSnapshot);
                                user.curRequestStatus = request.get(s);
                                users.add(user);
                            }
                        }
                    }
                    if(users.size() > 0){
                        UserAdapter userAdapter = new UserAdapter(users, this);
                        binding.notificationRecyclerView.setAdapter(userAdapter);
                        binding.notificationRecyclerView.setVisibility(View.VISIBLE);
                    }else{
                        showErrorMessage();
                    }
                }else {
                    showErrorMessage();
                }
                });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No notification yet"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), ReceiveActivity.class);
        intent.putExtra(UserProperties.KEY_USER, users);
        startActivity(intent);
        finish();
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