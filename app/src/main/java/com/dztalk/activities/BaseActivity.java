package com.dztalk.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.models.UserProperties;
import com.dztalk.utilities.LocalPreference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalPreference localPreference = new LocalPreference(getApplicationContext());
        FirebaseFirestore database  = FirebaseFirestore.getInstance();
        documentReference = database.collection(UserProperties.USER_DB_NAME)
                .document(localPreference.getString(UserProperties.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(UserProperties.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(UserProperties.KEY_AVAILABILITY, 1);
    }
}
