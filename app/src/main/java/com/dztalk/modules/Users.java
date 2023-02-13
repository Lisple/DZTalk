package com.dztalk.modules;

import java.io.Serializable;
import java.util.HashMap;

public class Users implements Serializable {
    public String firstname, lastname, fullname, image, email, gender, token, id, hometown, descriptions,status,location,job,birthday;
    public String curRequestStatus;
    public HashMap<String, String> contacts,request;

}
