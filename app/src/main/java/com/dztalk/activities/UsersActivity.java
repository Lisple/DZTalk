package com.dztalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.dztalk.R;
import com.dztalk.adapators.UserAdapter;
import com.dztalk.databinding.ActivityUsersBinding;
import com.dztalk.listeners.UserListener;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.GetUsers;
import com.dztalk.utilities.LocalPreference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private LocalPreference localPreference;
    BottomNavigationView bottomNavigationView;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());;
        setContentView(binding.getRoot());
        localPreference = new LocalPreference(getApplicationContext());
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setSelectedItemId(R.id.contact);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.chat:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
                case R.id.contact:
                    Toast.makeText(getApplicationContext(), "You are here", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
            }
            return true;
        });
        setNotificationNum();
        setListener();
        getUsers();
        mHandler.post(mRunnable);
    }

    private void setListener(){
        binding.fabSearch.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            finish();
        });
        binding.notification.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
            finish();
        });
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(UserProperties.USER_DB_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = localPreference.getString(UserProperties.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<Users> users = new ArrayList<>();
                        HashMap<String, String> contact = GetUsers.readFromSP(this,UserProperties.KEY_CONTACT);
                        for (String s: contact.keySet()){
                            if (contact.get(s).equals("accepted")){
                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                    if(queryDocumentSnapshot.getId().equals(s)){
                                        Users user = GetUsers.setUser(queryDocumentSnapshot);
                                        users.add(user);
                                    }
                                }
                            }
                        }
                        if(users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }else{
                            showEmptyMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void updateUsers(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(UserProperties.USER_DB_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = localPreference.getString(UserProperties.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<Users> users = new ArrayList<>();
                        HashMap<String, String> contact = GetUsers.readFromSP(this,UserProperties.KEY_CONTACT);
                        for (String s: contact.keySet()){
                            if (contact.get(s).equals("accepted")){
                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                    if(queryDocumentSnapshot.getId().equals(s)){
                                        Users user = GetUsers.setUser(queryDocumentSnapshot);
                                        users.add(user);
                                    }
                                }
                            }
                        }
                        if(users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void updateSelf(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(UserProperties.USER_DB_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = localPreference.getString(UserProperties.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(queryDocumentSnapshot.getId().equals(localPreference.getString(UserProperties.KEY_USER_ID))){
                                localPreference.putString(UserProperties.KEY_USER_ID, queryDocumentSnapshot.getId());
                                localPreference.putString(UserProperties.KEY_USER_Profile, queryDocumentSnapshot.getString(UserProperties.KEY_USER_Profile));
                                localPreference.putString(UserProperties.KEY_EMAIL, queryDocumentSnapshot.getString(UserProperties.KEY_EMAIL));
                                localPreference.putString(UserProperties.KEY_FIRSTNAME, queryDocumentSnapshot.getString(UserProperties.KEY_FIRSTNAME));
                                localPreference.putString(UserProperties.KEY_LASTNAME, queryDocumentSnapshot.getString(UserProperties.KEY_LASTNAME));
                                localPreference.putString(UserProperties.KEY_FULLNAME, queryDocumentSnapshot.getString(UserProperties.KEY_FULLNAME));
                                localPreference.putString(UserProperties.KEY_GENDER, queryDocumentSnapshot.getString(UserProperties.KEY_GENDER));
                                localPreference.putString(UserProperties.KEY_LOCATION, queryDocumentSnapshot.getString(UserProperties.KEY_LOCATION));
                                localPreference.putString(UserProperties.KEY_JOB, queryDocumentSnapshot.getString(UserProperties.KEY_JOB));
                                localPreference.putString(UserProperties.KEY_BIRTHDAY, queryDocumentSnapshot.getString(UserProperties.KEY_BIRTHDAY));
                                localPreference.putString(UserProperties.KEY_HOMETOWN, queryDocumentSnapshot.getString(UserProperties.KEY_HOMETOWN));
                                localPreference.putString(UserProperties.KEY_STATUS, queryDocumentSnapshot.getString(UserProperties.KEY_STATUS));
                                localPreference.putString(UserProperties.KEY_DESCRIPTION, queryDocumentSnapshot.getString(UserProperties.KEY_DESCRIPTION));
                                GetUsers.insertToSP(this,UserProperties.KEY_CONTACT,(HashMap<String, String>) queryDocumentSnapshot.get(UserProperties.KEY_CONTACT));
                                GetUsers.insertToSP(this,UserProperties.KEY_REQUEST,(HashMap<String, String>) queryDocumentSnapshot.get(UserProperties.KEY_REQUEST));
                            }
                        }
                    }
                });
    }

    private void setNotificationNum(){
        int count = 0;
        for(String s:GetUsers.readFromSP(this,UserProperties.KEY_REQUEST).values()){
            if (s.equals("pending")){
                count ++;
            }
        }
        System.out.println(("count:"+count));
        if(count == 0){
            binding.notificationContainer.setVisibility(View.INVISIBLE);
        }else{
            System.out.println("here");
            binding.notificationContainer.setVisibility(View.VISIBLE);
            if (count >= 99){
                binding.number.setText(String.valueOf(99));
            }else {
                binding.number.setText(String.valueOf(count));
            }
        }
    }

    private void showEmptyMessage(){
        binding.textErrorMessage.setText(String.format("%s","Go and add some new friends!"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
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
        Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
        intent.putExtra(UserProperties.KEY_USER, users);
        startActivity(intent);
        finish();
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            updateSelf();
            updateUsers();
            setNotificationNum();
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

}