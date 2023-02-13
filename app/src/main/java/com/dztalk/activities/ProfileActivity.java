package com.dztalk.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.R;
import com.dztalk.databinding.ActivityProfileBinding;
import com.dztalk.listeners.UserListener;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.LocalPreference;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity implements UserListener {
    private ActivityProfileBinding binding;
    BottomNavigationView bottomNavigationView;
    private LocalPreference localPreference;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        localPreference = new LocalPreference(getApplicationContext());
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.chat:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
                case R.id.contact:
                    startActivity(new Intent(getApplicationContext(), UsersActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
                case R.id.profile:
                    Toast.makeText(getApplicationContext(), "You are here", Toast.LENGTH_SHORT).show();
                    overridePendingTransition(0,0);
                    break;
            }
            return true;
        });


        loadInfor();
        setListeners();
    }


    public void loadInfor(){
        byte[] bytes = Base64.decode(localPreference.getString(UserProperties.KEY_USER_Profile),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.picture.setBackground(new BitmapDrawable(bitmap));
        binding.name.setText(localPreference.getString(UserProperties.KEY_FIRSTNAME)+" "+localPreference.getString(UserProperties.KEY_LASTNAME));
        binding.idContent.setText(localPreference.getString(UserProperties.KEY_USER_ID));
        binding.genderContent.setText(localPreference.getString(UserProperties.KEY_GENDER));
        binding.emailContent.setText(localPreference.getString(UserProperties.KEY_EMAIL));
        binding.locationContent.setText(localPreference.getString(UserProperties.KEY_LOCATION));
        binding.hometownContent.setText(localPreference.getString(UserProperties.KEY_HOMETOWN));
        binding.jobContent.setText(localPreference.getString(UserProperties.KEY_JOB));
        binding.descriptionContent.setText(localPreference.getString(UserProperties.KEY_DESCRIPTION));
        binding.birthdayContent.setText(localPreference.getString(UserProperties.KEY_BIRTHDAY));
        binding.status.setText(localPreference.getString(UserProperties.KEY_STATUS));

    }

    private void setListeners(){
        binding.edit.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), ProfileEditActivity.class)));
    }

    @Override
    public void onUserClicked(Users users) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}