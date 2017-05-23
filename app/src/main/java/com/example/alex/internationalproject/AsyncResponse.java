package com.example.alex.internationalproject;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Alex on 22-3-2017.
 */

public interface AsyncResponse {
    void processFinish(JSONArray output);

    void processFinish(String result);

    void addDeviceFinish(JSONObject json);

    void getDeviceListFinish(JSONArray json);
}
