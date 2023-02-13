package com.dztalk.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;


import com.dztalk.R;
import com.dztalk.adapators.RecentChatAdapter;
import com.dztalk.databinding.ActivityMainBinding;
import com.dztalk.listeners.ChatListener;
import com.dztalk.models.ChatMessage;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.LocalPreference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ChatListener {
    private ActivityMainBinding binding;
    private LocalPreference localPreference;
    private List<ChatMessage> chatMessageList;
    private RecentChatAdapter chatAdapter;
    private FirebaseFirestore db;

    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        localPreference =  new LocalPreference(getApplicationContext());
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setSelectedItemId(R.id.chat);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.chat:
                    Toast.makeText(getApplicationContext(), "You are here", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.contact:
                    startActivity(new Intent(getApplicationContext(), UsersActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
            }
            return true;
        });
        initChats();
        loadUserDetails();
        getToken();
        setListener();
        listenChats();
    }

    private void initChats(){
        chatMessageList = new ArrayList<>();
        chatAdapter = new RecentChatAdapter(chatMessageList, this);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }


    private void setListener() {
        binding.imageSignOut.setOnClickListener(view -> signOut());
    }

    private void loadUserDetails(){
        binding.textName.setText("Hi "+localPreference.getString(UserProperties.KEY_LASTNAME));
        byte[] bytes = Base64.decode(localPreference.getString(UserProperties.KEY_USER_Profile),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }


    private void updateToken(String token){
        localPreference.putString(UserProperties.KEY_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(UserProperties.USER_DB_NAME).document(
                        localPreference.getString(UserProperties.KEY_USER_ID)
                );
        documentReference.update(UserProperties.KEY_TOKEN, token)
                .addOnSuccessListener(unused -> System.out.println("Token Updated"))
                .addOnFailureListener(e -> showToast("Fail to update token"));
    }

    private void signOut(){
        showToast("Signing out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(UserProperties.USER_DB_NAME).document(
                        localPreference.getString(UserProperties.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(UserProperties.KEY_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    localPreference.clear();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e->showToast("Failed to sign out"));

    }

    private void listenChats(){
        db.collection((UserProperties.KEY_COLLECTION_RECENT_CHATS))
                .whereEqualTo(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(UserProperties.KEY_COLLECTION_RECENT_CHATS)
                .whereEqualTo(UserProperties.KEY_RECEIVER_ID, localPreference.getString(UserProperties.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return ;
        }
        // get the chats
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){

                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(UserProperties.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(UserProperties.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (localPreference.getString(UserProperties.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(UserProperties.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(UserProperties.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(UserProperties.KEY_RECEIVER_ID);
                    }
                    else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(UserProperties.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(UserProperties.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(UserProperties.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(UserProperties.KEY_LAST_MESSAGE);
                    chatMessage.dataObject = documentChange.getDocument().getDate(UserProperties.KEY_TIMESTAMP);
                    chatMessage.type = documentChange.getDocument().getString(UserProperties.KEY_CHAT_TYPE);
                    chatMessageList.add(chatMessage);
                }

                else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < chatMessageList.size(); i ++) {
                        String senderId = documentChange.getDocument().getString(UserProperties.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(UserProperties.KEY_RECEIVER_ID);
                        if (chatMessageList.get(i).senderId.equals(senderId) && chatMessageList.get(i).receiverId.equals(receiverId)){
                            chatMessageList.get(i).message = documentChange.getDocument().getString(UserProperties.KEY_LAST_MESSAGE);
                            chatMessageList.get(i).dataObject = documentChange.getDocument().getDate(UserProperties.KEY_TIMESTAMP);
                            chatMessageList.get(i).type = documentChange.getDocument().getString(UserProperties.KEY_CHAT_TYPE);
                            break;
                        }
                    }
                }
            }

            // sort the chats and show
            Collections.sort(chatMessageList, (x, y) -> y.dataObject.compareTo(x.dataObject));
            chatAdapter.notifyDataSetChanged();
            binding.chatRecyclerView.smoothScrollToPosition(0);
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
            binding.recentChatProcessBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void onChatClicked(Users user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(UserProperties.KEY_USER, user);
        startActivity(intent);
    }
}