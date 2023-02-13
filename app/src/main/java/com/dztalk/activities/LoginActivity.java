package com.dztalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.databinding.ActivityLoginBinding;
import com.dztalk.models.UserProperties;
import com.dztalk.utilities.GetUsers;
import com.dztalk.utilities.LocalPreference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LocalPreference localPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        localPreference = new LocalPreference(getApplicationContext());
        setContentView(binding.getRoot());
        listener();
    }

    /**
     * The listener to listen what is going on
     */
    private void listener() {
        // jump to register
        binding.registerHere.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), EnrollActivity.class)));
        // login
        binding.buttonLogin.setOnClickListener(v -> {
            // login after checking the input
            if (isValidLoginCheck()) login();
        });
        binding.forgetPassword.setOnClickListener(v ->startActivity(new Intent(getApplicationContext(), PasswordActivity.class)) );
    }

    /**
     * Show error message to user through toast
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Check whether the login is legal
     * @return
     */
    private Boolean isValidLoginCheck() {
        // get the input
        String email = binding.loginInputEmail.getText().toString();
        String password = binding.loginInputPassword.getText().toString();
        // check the email
        if (email.trim().isEmpty()) {
            showToast("'Email' missing");
            return false;
        }
        // check the format of email
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showToast("Please enter valid email");
            return false;
        }
        // check the password
        else if (password.trim().isEmpty()) {
            showToast("'Password' missing");
            return false;
        }
        return true;
    }

    /**
     * Login with database
     */
    private void login(){
        loadingPage(true);
        // get the database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // get the collection
        CollectionReference currentCollection = db.collection(UserProperties.USER_DB_NAME);

        db.collection(UserProperties.USER_DB_NAME)
                .whereEqualTo(UserProperties.KEY_EMAIL, binding.loginInputEmail.getText().toString())
                .whereEqualTo(UserProperties.KEY_PASSWORD, binding.loginInputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    // login successfully
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        // get the information
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        // store the information to local preference
                        localPreference.putBoolean("IsSignIN", true);
                        localPreference.putString(UserProperties.KEY_USER_ID, documentSnapshot.getId());
                        localPreference.putString(UserProperties.KEY_USER_Profile, documentSnapshot.getString(UserProperties.KEY_USER_Profile));
                        localPreference.putString(UserProperties.KEY_EMAIL, documentSnapshot.getString(UserProperties.KEY_EMAIL));
                        localPreference.putString(UserProperties.KEY_FIRSTNAME, documentSnapshot.getString(UserProperties.KEY_FIRSTNAME));
                        localPreference.putString(UserProperties.KEY_LASTNAME, documentSnapshot.getString(UserProperties.KEY_LASTNAME));
                        localPreference.putString(UserProperties.KEY_FULLNAME, documentSnapshot.getString(UserProperties.KEY_FULLNAME));
                        localPreference.putString(UserProperties.KEY_GENDER, documentSnapshot.getString(UserProperties.KEY_GENDER));
                        localPreference.putString(UserProperties.KEY_LOCATION, documentSnapshot.getString(UserProperties.KEY_LOCATION));
                        localPreference.putString(UserProperties.KEY_JOB, documentSnapshot.getString(UserProperties.KEY_JOB));
                        localPreference.putString(UserProperties.KEY_BIRTHDAY, documentSnapshot.getString(UserProperties.KEY_BIRTHDAY));
                        localPreference.putString(UserProperties.KEY_HOMETOWN, documentSnapshot.getString(UserProperties.KEY_HOMETOWN));
                        localPreference.putString(UserProperties.KEY_STATUS, documentSnapshot.getString(UserProperties.KEY_STATUS));
                        localPreference.putString(UserProperties.KEY_DESCRIPTION, documentSnapshot.getString(UserProperties.KEY_DESCRIPTION));
                        GetUsers.insertToSP(this,UserProperties.KEY_CONTACT,(HashMap<String, String>) documentSnapshot.get(UserProperties.KEY_CONTACT));
                        GetUsers.insertToSP(this,UserProperties.KEY_REQUEST,(HashMap<String, String>) documentSnapshot.get(UserProperties.KEY_REQUEST));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    // login fail
                    else {
                        showToast("Fail to login. Please check your email and password.");
                        loadingPage(false);
                    }
                });
    }


    private void loadingPage(Boolean isLoading){
        if (isLoading){
            binding.buttonLogin.setVisibility(View.INVISIBLE);
            binding.loadingBar.setVisibility(View.VISIBLE);
        }else{
            binding.buttonLogin.setVisibility(View.VISIBLE);
            binding.loadingBar.setVisibility(View.INVISIBLE);
        }
    }
}