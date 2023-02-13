package com.dztalk.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.dztalk.R;
import com.dztalk.databinding.ActivityEditProfileBinding;
import com.dztalk.listeners.UserListener;
import com.dztalk.models.UserProperties;
import com.dztalk.modules.Users;
import com.dztalk.utilities.LocalPreference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity implements UserListener {
    private ActivityEditProfileBinding binding;
    private LocalPreference localPreference;
    private String enrollProfile;
    private String myIp;
    private String myLocation;
    private String myBirthday;
    private DatePickerDialog datePickerDialog;
    private Button selectDateButton;
    private String myProfile;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        localPreference = new LocalPreference(getApplicationContext());
        myLocation = localPreference.getString(UserProperties.KEY_LOCATION);
        myBirthday = localPreference.getString(UserProperties.KEY_BIRTHDAY);
        myProfile = localPreference.getString(UserProperties.KEY_USER_Profile);
        setContentView(binding.getRoot());
        binding.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    showToast("Locating, please wait...");
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    URL my_ip = new URL("https://checkip.amazonaws.com/");
                    URLConnection connection = my_ip.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    BufferedReader ip = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    myIp = ip.readLine();
                    System.out.println(myIp);
                    String searchURL = "https://ip.taobao.com/outGetIpInfo?ip=" + myIp;
                    String path = "&accessKey=alibaba-inc";
                    String returnStr = getResultByUrl(searchURL, path, "utf-8");
                    JSONObject json = null;
                    json = new JSONObject(returnStr);
                    System.out.println(returnStr);

                    if (returnStr != null && "0".equals(json.get("code").toString())) {
                        String country = decodeUnicode(json.optJSONObject("data").getString("country_id"));
                        if (country != null) {
                            if(!country.equals("CN")) {
                                myLocation = decodeUnicode(json.optJSONObject("data").getString("country_id"));
                                binding.locationContent.setText(myLocation);
                            }else{
                                myLocation = decodeUnicode(json.optJSONObject("data").getString("region"));
                                binding.locationContent.setText(myLocation);
                                showToast("According to local regulation, your location will be account in PROVINCE.");
                            }

                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Internet Connection Failed");
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        loadInfor();
        listener();
        initDatePick();
        selectDateButton = findViewById(R.id.selectDate);
        if(myBirthday.equals("Unknown")){
            selectDateButton.setText(getTodaysDate());
        }else{
            selectDateButton.setText(myBirthday);
        }

    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month += 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePick() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;
                String date = makeDateString(day, month, year);
                selectDateButton.setText(date);
                myBirthday = date;
            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month) {
        if(month == 1){
            return "JAN";
        }else if(month == 2){
            return "Feb";
        }else if(month == 3){
            return "MAR";
        }else if(month == 4){
            return "APR";
        }else if(month == 5){
            return "MAY";
        }else if(month == 6){
            return "JUN";
        }else if(month == 7){
            return "JUL";
        }else if(month == 8){
            return "AUG";
        }else if(month == 9){
            return "SEP";
        }else if(month == 10){
            return "OCT";
        }else if(month == 11){
            return "NOV";
        }else if(month == 12){
            return "DEC";
        }
        return "UNKNOWN";
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
        binding.status.setText(localPreference.getString(UserProperties.KEY_STATUS));
    }


    public void updateProfile(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference profileInformation =
                db.collection(UserProperties.USER_DB_NAME).document(
                        localPreference.getString(UserProperties.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();


        updates.put(UserProperties.KEY_GENDER, binding.genderContent.getText().toString());
        localPreference.putString(UserProperties.KEY_GENDER, binding.genderContent.getText().toString());
        updates.put(UserProperties.KEY_HOMETOWN, binding.hometownContent.getText().toString());
        localPreference.putString(UserProperties.KEY_HOMETOWN, binding.hometownContent.getText().toString());
        updates.put(UserProperties.KEY_JOB, binding.jobContent.getText().toString());
        localPreference.putString(UserProperties.KEY_JOB, binding.jobContent.getText().toString());
        updates.put(UserProperties.KEY_EMAIL, binding.emailContent.getText().toString());
        localPreference.putString(UserProperties.KEY_EMAIL, binding.emailContent.getText().toString());
        updates.put(UserProperties.KEY_DESCRIPTION, binding.descriptionContent.getText().toString());
        localPreference.putString(UserProperties.KEY_DESCRIPTION, binding.descriptionContent.getText().toString());
        updates.put(UserProperties.KEY_LOCATION, myLocation);
        localPreference.putString(UserProperties.KEY_LOCATION, myLocation);
        updates.put(UserProperties.KEY_BIRTHDAY, myBirthday);
        localPreference.putString(UserProperties.KEY_BIRTHDAY, myBirthday);
        updates.put(UserProperties.KEY_STATUS, binding.status.getText().toString());
        localPreference.putString(UserProperties.KEY_STATUS, binding.status.getText().toString());
        updates.put(UserProperties.KEY_USER_Profile, myProfile);
        localPreference.putString(UserProperties.KEY_USER_Profile, myProfile);


        profileInformation.update(updates)
                .addOnSuccessListener(unused -> System.out.println("The profile has been updated successfully!"))
                .addOnFailureListener(e -> System.out.println("Failed to update the profile! Please try it again!"));

    }

    private void listener(){
        // listen the 'Register' button
        binding.submit.setOnClickListener(v -> {
            System.out.println("Submit button clicked!");

            updateProfile();

            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

        });

        binding.picture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            selectImage.launch(intent);
        });
    }

    /**
     * Let the user to pick image
     */
    private final ActivityResultLauncher<Intent> selectImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.picture.setImageBitmap(bitmap);
                            enrollProfile = encodeImage(bitmap);
                            myProfile = enrollProfile;
                        }catch (FileNotFoundException e){
                            showToast(e.toString());
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap){
        int width = 150;
        int height = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private Boolean isValidRegisterCheck() {
        // check all the blanks are filled up
        if (binding.picture == null) {
            showToast("Profile missing");
            return false;
        }else if(binding.birthday.getText().toString().trim().isEmpty()){
            showToast("Profile missing");
            return false;
        }else if(binding.status.getText().toString().trim().isEmpty()){
            showToast("Status missing");
            return false;
        }else if(binding.gender.getText().toString().trim().isEmpty()){
            showToast("Gender missing");
            return false;
        }else if(binding.hometown.getText().toString().trim().isEmpty()){
            showToast("Hometown missing");
            return false;
        }else if(binding.birthday.getText().toString().trim().isEmpty()){
            showToast("Birthday missing");
            return false;
        }else if(binding.email.getText().toString().trim().isEmpty()){
            showToast("Email missing");
            return false;
        }else if(binding.job.getText().toString().trim().isEmpty()){
            showToast("Job missing");
            return false;
        }else if(binding.description.getText().toString().trim().isEmpty()){
            showToast("Description missing");
            return false;
        }

        return true;
    }

    public void showTimePickerDialog(View v) {
        datePickerDialog.show();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static String getResultByUrl(String path, String params, String encoding) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(params);
            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            return buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer buffer = new StringBuffer(len);
        for (int i = 0; i < len;) {
            aChar = theString.charAt(i++);
            if (aChar == '\\') {
                aChar = theString.charAt(i++);
                if (aChar == 'u') {
                    int val = 0;
                    for (int j = 0; j < 4; j++) {
                        aChar = theString.charAt(i++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                val = (val << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                val = (val << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                val = (val << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed  encoding.");
                        }
                    }
                    buffer.append((char) val);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    }
                    if (aChar == 'r') {
                        aChar = '\r';
                    }
                    if (aChar == 'n') {
                        aChar = '\n';
                    }
                    if (aChar == 'f') {
                        aChar = '\f';
                    }
                    buffer.append(aChar);
                }
            } else {
                buffer.append(aChar);
            }
        }
        return buffer.toString();
    }

        @Override
    public void onUserClicked(Users users) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
