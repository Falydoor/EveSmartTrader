package com.smarttrader.domain.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import org.springframework.stereotype.Component;

/**
 * Created by Theo on 8/22/16.
 */
@Component
public class GsonBean {

    private Gson gson;

    public GsonBean() {
        gson = new GsonBuilder().create();
    }

    public JsonArray parse(String json) {
        return gson.fromJson(json, JsonArray.class);
    }

}
