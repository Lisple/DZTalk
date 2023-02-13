package com.dztalk.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.dztalk.databinding.ActivityPasswordBinding;
import com.dztalk.models.UserProperties;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PasswordActivity extends AppCompatActivity {
    private ActivityPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        listener();
    }

    private void listener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.btnReset.setOnClickListener(v -> {
            if (isValidUpdateCheck()) updatePassword();
        });
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
    private Boolean isValidUpdateCheck() {
        // get the input
        String email = binding.loginInputEmail.getText().toString();
        String old = binding.loginOldInputPassword.getText().toString();
        String password = binding.loginInputPassword.getText().toString();
        String confirm = binding.loginInputConfirmPassword.getText().toString();
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
        else if (old.trim().isEmpty()) {
            showToast("'Old Password' missing");
            return false;
        }
        // check the password
        else if (password.trim().isEmpty()) {
            showToast("'New Password' missing");
            return false;
        }
        else if (confirm.trim().isEmpty()) {
            showToast("'Confirm New Password' missing");
            return false;
        }
        return true;
    }

    /**
     * Login with database
     */
    private void updatePassword(){
        loadingPage(true);
        // get the database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // get the collection
        CollectionReference currentCollection = db.collection(UserProperties.USER_DB_NAME);

        db.collection(UserProperties.USER_DB_NAME)
                .whereEqualTo(UserProperties.KEY_EMAIL, binding.loginInputEmail.getText().toString())
                .whereEqualTo(UserProperties.KEY_PASSWORD, binding.loginOldInputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    // login successfully
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        // get the information
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        if(documentSnapshot.getString(UserProperties.KEY_PASSWORD).equals(binding.loginInputConfirmPassword.getText().toString())){
                            showToast("Password cannot be the same!");
                            loadingPage(false);
                        } else if(binding.loginInputPassword.getText().toString().equals(binding.loginInputConfirmPassword.getText().toString())){
                            DocumentReference sender =
                                    db.collection(UserProperties.USER_DB_NAME).document(
                                            documentSnapshot.getId()
                                    );
                            sender.update(UserProperties.KEY_PASSWORD,binding.loginInputConfirmPassword.getText().toString());
                            showToast("Update password successfully.");
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            showToast("Passwords don't match. Please check");
                            loadingPage(false);
                        }
                    }
                    // update fail
                    else {
                        showToast("Password or Email doesn't match. Fail to update password");
                        loadingPage(false);
                    }
                });
    }


    private void loadingPage(Boolean isLoading){
        if (isLoading){
            binding.btnReset.setVisibility(View.INVISIBLE);
            binding.loadingBar.setVisibility(View.VISIBLE);
        }else{
            binding.btnReset.setVisibility(View.VISIBLE);
            binding.loadingBar.setVisibility(View.INVISIBLE);
        }
    }
}