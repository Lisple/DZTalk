package com.dztalk.activities;


import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dztalk.adapators.ChatAdapter;
import com.dztalk.databinding.ActivityChatBinding;
import com.dztalk.models.ChatMessage;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.network.ApiClient;
import com.dztalk.network.ApiService;
import com.dztalk.utilities.LocalPreference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private Users receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private LocalPreference localPreference;
    private FirebaseFirestore database;
    private String chatId = null;
    private Boolean isReceiverAvailable = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (isCameraExist()) {
            checkPermission();
        }
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init() {
        localPreference = new LocalPreference(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                localPreference.getString(UserProperties.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
        message.put(UserProperties.KEY_RECEIVER_ID, receiverUser.id);
        message.put(UserProperties.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(UserProperties.KEY_TIMESTAMP, new Date());
        database.collection(UserProperties.KEY_COLLECTION_CHAT).add(message);
        // record the message
        if (chatId != null) {
            updateChat(binding.inputMessage.getText().toString(), "message");
        } else {
            HashMap<String, Object> chatList = new HashMap<>();
            chatList.put(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
            chatList.put(UserProperties.KEY_SENDER_NAME, localPreference.getString(UserProperties.KEY_FULLNAME));
            chatList.put(UserProperties.KEY_SENDER_IMAGE, localPreference.getString(UserProperties.KEY_USER_Profile));
            chatList.put(UserProperties.KEY_RECEIVER_ID, receiverUser.id);
            chatList.put(UserProperties.KEY_RECEIVER_NAME, receiverUser.firstname + " " + receiverUser.lastname);
            chatList.put(UserProperties.KEY_RECEIVER_IMAGE, receiverUser.image);
            chatList.put(UserProperties.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            chatList.put(UserProperties.KEY_TIMESTAMP, new Date());
            addChat(chatList);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(UserProperties.KEY_USER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
                data.put(UserProperties.KEY_FULLNAME, localPreference.getString(UserProperties.KEY_FULLNAME));
                data.put(UserProperties.KEY_TOKEN, localPreference.getString(UserProperties.KEY_TOKEN));
                data.put(UserProperties.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(UserProperties.REMOTE_MSG_DATA, data);
                body.put(UserProperties.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void sendMessage(String msg) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
        message.put(UserProperties.KEY_RECEIVER_ID, receiverUser.id);
        message.put(UserProperties.KEY_MESSAGE, msg);
        message.put(UserProperties.KEY_TIMESTAMP, new Date());
        message.put(UserProperties.KEY_CHAT_TYPE, "location");
        database.collection(UserProperties.KEY_COLLECTION_CHAT).add(message);
        // record the message
        if (chatId != null) {
            updateChat(msg, "location");
        } else {
            HashMap<String, Object> chatList = new HashMap<>();
            chatList.put(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
            chatList.put(UserProperties.KEY_SENDER_NAME, localPreference.getString(UserProperties.KEY_FULLNAME));
            chatList.put(UserProperties.KEY_SENDER_IMAGE, localPreference.getString(UserProperties.KEY_USER_Profile));
            chatList.put(UserProperties.KEY_RECEIVER_ID, receiverUser.id);
            chatList.put(UserProperties.KEY_RECEIVER_NAME, receiverUser.firstname + " " + receiverUser.lastname);
            chatList.put(UserProperties.KEY_RECEIVER_IMAGE, receiverUser.image);
            chatList.put(UserProperties.KEY_LAST_MESSAGE, msg);
            chatList.put(UserProperties.KEY_TIMESTAMP, new Date());
            chatList.put(UserProperties.KEY_CHAT_TYPE, "location");
            addChat(chatList);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(UserProperties.KEY_USER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
                data.put(UserProperties.KEY_FULLNAME, localPreference.getString(UserProperties.KEY_FULLNAME));
                data.put(UserProperties.KEY_TOKEN, localPreference.getString(UserProperties.KEY_TOKEN));
                data.put(UserProperties.KEY_MESSAGE, "Location Information");

                JSONObject body = new JSONObject();
                body.put(UserProperties.REMOTE_MSG_DATA, data);
                body.put(UserProperties.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void sendImage(String imageCode) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
        message.put(UserProperties.KEY_RECEIVER_ID, receiverUser.id);
        message.put(UserProperties.KEY_MESSAGE, imageCode);
        message.put(UserProperties.KEY_TIMESTAMP, new Date());
        message.put(UserProperties.KEY_CHAT_TYPE, "image");
        database.collection(UserProperties.KEY_COLLECTION_CHAT).add(message);
        // record the message
        if (chatId != null) {
            updateChat(imageCode, "image");
        } else {
            HashMap<String, Object> chatList = new HashMap<>();
            chatList.put(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
            chatList.put(UserProperties.KEY_SENDER_NAME, localPreference.getString(UserProperties.KEY_FULLNAME));
            chatList.put(UserProperties.KEY_SENDER_IMAGE, localPreference.getString(UserProperties.KEY_USER_Profile));
            chatList.put(UserProperties.KEY_RECEIVER_ID, receiverUser.id);
            chatList.put(UserProperties.KEY_RECEIVER_NAME, receiverUser.firstname + " " + receiverUser.lastname);
            chatList.put(UserProperties.KEY_RECEIVER_IMAGE, receiverUser.image);
            chatList.put(UserProperties.KEY_LAST_MESSAGE, imageCode);
            chatList.put(UserProperties.KEY_TIMESTAMP, new Date());
            chatList.put(UserProperties.KEY_CHAT_TYPE, "image");
            addChat(chatList);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(UserProperties.KEY_USER_ID, localPreference.getString(UserProperties.KEY_USER_ID));
                data.put(UserProperties.KEY_FULLNAME, localPreference.getString(UserProperties.KEY_FULLNAME));
                data.put(UserProperties.KEY_TOKEN, localPreference.getString(UserProperties.KEY_TOKEN));
                data.put(UserProperties.KEY_MESSAGE, "Image Message");

                JSONObject body = new JSONObject();
                body.put(UserProperties.REMOTE_MSG_DATA, data);
                body.put(UserProperties.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
    }


    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                UserProperties.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                } else {
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        database.collection(UserProperties.USER_DB_NAME).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(UserProperties.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(UserProperties.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(UserProperties.KEY_TOKEN);
                if (receiverUser.image == null) {
                    receiverUser.image = value.getString(UserProperties.KEY_RECEIVER_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }

        });
    }

    private void listenMessages() {
        database.collection(UserProperties.KEY_COLLECTION_CHAT)
                .whereEqualTo(UserProperties.KEY_SENDER_ID, localPreference.getString(UserProperties.KEY_USER_ID))
                .whereEqualTo(UserProperties.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(UserProperties.KEY_COLLECTION_CHAT)
                .whereEqualTo(UserProperties.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(UserProperties.KEY_RECEIVER_ID, localPreference.getString(UserProperties.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(UserProperties.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(UserProperties.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(UserProperties.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(UserProperties.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(UserProperties.KEY_TIMESTAMP);
                    chatMessage.type = documentChange.getDocument().getString(UserProperties.KEY_CHAT_TYPE);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dataObject.compareTo(obj2.dataObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        // get chat id
        if (chatId == null) {
            checkForChat();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }

    }

    private void loadReceiverDetails() {
        receiverUser = (Users) getIntent().getSerializableExtra(UserProperties.KEY_USER);
        binding.textName.setText(receiverUser.fullname);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.sendImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            selectImage.launch(intent);
        });
        binding.takeImage.setOnClickListener(v -> {
            checkPermission();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 100);
        });
        binding.sendLocation.setOnClickListener(v -> {
            askLocationPermissions();
        });
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addChat(HashMap<String, Object> currChat) {
        database.collection(UserProperties.KEY_COLLECTION_RECENT_CHATS)
                .add(currChat)
                .addOnSuccessListener(documentReference -> chatId = documentReference.getId());
    }

    private void updateChat(String message, String type) {
        DocumentReference documentReference =
                database.collection(UserProperties.KEY_COLLECTION_RECENT_CHATS)
                        .document(chatId);
        documentReference.update(
                UserProperties.KEY_LAST_MESSAGE, message,
                UserProperties.KEY_TIMESTAMP, new Date(),
                UserProperties.KEY_CHAT_TYPE, type
        );
    }

    private void checkForChat() {
        if (chatMessages.size() != 0) {
            checkForChatRemotely(
                    localPreference.getString(UserProperties.KEY_USER_ID),
                    receiverUser.id
            );
            checkForChatRemotely(
                    receiverUser.id,
                    localPreference.getString(UserProperties.KEY_USER_ID)
            );
        }
    }

    private void checkForChatRemotely(String senderId, String receiverId) {
        database.collection(UserProperties.KEY_COLLECTION_RECENT_CHATS)
                .whereEqualTo(UserProperties.KEY_SENDER_ID, senderId)
                .whereEqualTo(UserProperties.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(chatOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> chatOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null
                && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            chatId = documentSnapshot.getId();
        }
    };

    /**
     * Encode the image to store it
     * @return
     */
    private String encodeImage(Bitmap bitmap) {
        int width = 1000;
        int height = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Let the user to pick image
     */
    private final ActivityResultLauncher<Intent> selectImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            sendImage(encodeImage(bitmap));
                        } catch (FileNotFoundException e) {
                            showToast(e.toString());
                        }
                    }
                }
            }
    );


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                sendImage(encodeImage(bitmap));
            }
        }
    }

    private void askLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            getLocation();
        }
    }


    // Get Location part
    @SuppressLint("MissingPermission")
    private void getLocation() {

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Initialize geoCoder
                    Geocoder geocoder = new Geocoder(ChatActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String msg = "My location is at  " + addresses.get(0).getAddressLine(0) + " with latitude at: " + location.getLatitude() + " and longitude at " + location.getLongitude();
                        sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("Cannot get location");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Cannot get location on this device");
            }
        });
    }

    private boolean isCameraExist() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    /**
     * Show error message to user through toast
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}