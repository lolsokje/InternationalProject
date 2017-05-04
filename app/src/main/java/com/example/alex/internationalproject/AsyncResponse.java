package com.example.alex.internationalproject;

import org.json.JSONArray;

/**
 * Created by Alex on 22-3-2017.
 */

public interface AsyncResponse {
    void processFinish(JSONArray output);

    void processFinish(String result);
}
