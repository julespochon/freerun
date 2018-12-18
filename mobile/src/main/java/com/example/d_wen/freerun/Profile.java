package com.example.d_wen.freerun;

import java.io.Serializable;

public class Profile implements Serializable {

    protected String username;
    protected String password;
    protected int height;
    protected float weight;
    protected String photoPath;

   public Profile(String username, String password) {
        this.username = username;
        this.password = password;
    }
}