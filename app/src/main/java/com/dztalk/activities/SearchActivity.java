package com.dztalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.dztalk.adapators.UserAdapter;
import com.dztalk.databinding.ActivitySearchBinding;
import com.dztalk.listeners.UserListener;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.GetUsers;
import com.dztalk.utilities.LocalPreference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements UserListener {
    private ActivitySearchBinding binding;
    private LocalPreference localPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        localPreference = new LocalPreference(getApplicationContext());
        searchFriend();
        setListener();
    }

    private void setListener(){
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }

    private void searchFriend() {
        binding.searchFriend.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()==0){
                    if(binding.textErrorMessage.getVisibility() == View.VISIBLE){
                        binding.textErrorMessage.setVisibility(View.INVISIBLE);
                    }
                    if(binding.usersRecyclerView.getVisibility() == View.VISIBLE){
                        binding.usersRecyclerView.setVisibility(View.INVISIBLE);
                    }
                }
                getUsers(newText);
                return false;
            }
        });
    }

    private void getUsers(String s){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(UserProperties.USER_DB_NAME)
            .get()
            .addOnCompleteListener(task -> {
                if(s.length()!=0){
                    loading(false);
                    String currentUserId = localPreference.getString(UserProperties.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<Users> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            String fullName = queryDocumentSnapshot.getString(UserProperties.KEY_FIRSTNAME)+" "+queryDocumentSnapshot.getString(UserProperties.KEY_LASTNAME);
                            String email  = queryDocumentSnapshot.getString(UserProperties.KEY_EMAIL);
                            if(fullName.toLowerCase().contains(s.toLowerCase()) || email.contains(s.toLowerCase())){
                                Users user = GetUsers.setUser(queryDocumentSnapshot);
                                users.add(user);
                            }
                        }
                        if(users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                            if(binding.textErrorMessage.getVisibility() == View.VISIBLE){
                                binding.textErrorMessage.setVisibility(View.INVISIBLE);
                            }
                        }else{
                            showErrorMessage();
                            if(binding.usersRecyclerView.getVisibility() == View.VISIBLE){
                                binding.usersRecyclerView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }else {
                        showErrorMessage();
                        if(binding.usersRecyclerView.getVisibility() == View.VISIBLE){
                            binding.usersRecyclerView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }


    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user found"));
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
        Intent intent = new Intent(getApplicationContext(), RequestActivity.class);
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