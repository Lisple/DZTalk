package com.dztalk.models;

import java.util.HashMap;

public class UserProperties {
    public static String KEY_USER_ID = "USER_ID";
    public static String KEY_USER_Profile = "USER_PROFILE";
    public static String KEY_FIRSTNAME = "FIRST_NAME";
    public static String KEY_LASTNAME = "LAST_NAME";
    public static String KEY_FULLNAME = "FULL_NAME";
    public static String KEY_GENDER = "GENDER";
    public static String KEY_EMAIL = "EMAIL";
    public static String KEY_PASSWORD = "PASSWORD";
    public static String KEY_CONTACT = "CONTACTS";
    public static String KEY_REQUEST = "REQUEST";
    public static String KEY_TOKEN = "TOKEN";
    public static String KEY_USER = "user";

    public static String KEY_COLLECTION_CHAT = "chat";
    public static String KEY_SENDER_ID = "sender_ID";
    public static String KEY_RECEIVER_ID = "receiver_ID";
    public static String KEY_MESSAGE = "message";
    public static String KEY_TIMESTAMP = "timestamp";

    public static String USER_DB_NAME = "users_information";
    public static String KEY_LOCATION = "LOCATION";
    public static String KEY_HOMETOWN = "HOMETOWN";
    public static String KEY_BIRTHDAY = "BIRTHDAY";
    public static String KEY_STATUS = "STATUS";
    public static String KEY_JOB = "JOB";
    public static String KEY_DESCRIPTION = "DESCRIPTION";
    public static String KEY_CHAT_TYPE = "TYPE";

    // preference
    public static String KEY_PREFERENCE = "DZTalk";
    public static Boolean IsSignIN = false;

    // recent chats
    public static String KEY_COLLECTION_RECENT_CHATS = "recent_chats";
    public static String KEY_SENDER_NAME = "RECENT_MESS_SENDER_NAME";
    public static String KEY_RECEIVER_NAME = "RECENT_MESS_RECEIVER_NAME";
    public static String KEY_SENDER_IMAGE = "RECENT_MESS_SENDER_IMAGE";
    public static String KEY_RECEIVER_IMAGE = "RECENT_MESS_RECEIVER_IMAGE";
    public static String KEY_LAST_MESSAGE = "LAST_MESS";

    // user availability
    public static String KEY_AVAILABILITY = "availability";
    public static String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static String REMOTE_MSG_DATA = "data";
    public static String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAnmjJDuw:APA91bENRj0YCrmILSUeRHnBJjxqxL6lqeCr2iJfxJ1W3AECjj9wWvrvmpbOL5KTcbe3KGM8fip3y_XEO9ChGxeHE0rUzVTPJmJ0II6elYglazCTim17MeTrRf18Y2lTCOUxmhydAyRI"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
