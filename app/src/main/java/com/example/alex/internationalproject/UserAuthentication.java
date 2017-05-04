package com.example.alex.internationalproject;

import java.net.URL;

/**
 * Created by Alex on 4-5-2017.
 */

public class UserAuthentication {
    private URL url;

    /*
    * Constructor for empty UserAuthentication object
    * */
    public UserAuthentication() {
        // TODO implement
    }

    /*
    * Constructor for UserAuthentication object used for signing up and signing in
    * */
    public UserAuthentication(String name, String password, String action) {
        if(action == "signup") {
            try {
                url = new URL("http://141.135.5.117:3500/user/signup");
            } catch(Exception e) {
                e.printStackTrace();
            }


        }
    }


    /*
    * Constructor for UserAuthentication object used for authentication with a token
    * */
    public UserAuthentication(String token) {
        // TODO implement
    }
}
