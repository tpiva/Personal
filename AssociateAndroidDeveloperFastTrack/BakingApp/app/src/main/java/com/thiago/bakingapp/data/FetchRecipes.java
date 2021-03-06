/*
 * Create by Thiago Piva Magalhães on  01/07/17 20:41
 * Copyright (c) 2017. All right reserved.
 * File FetchRecipes.java belongs to Project BakingApp
 *
 * Last modified 25/06/17 12:58
 *
 */
package com.thiago.bakingapp.data;

import android.util.Log;

import com.thiago.bakingapp.bean.Recipe;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Communicate with webservice to get recipe data.
 */
public class FetchRecipes {

    private static final String TAG = FetchRecipes.class.getSimpleName();
    private static final String BAKING_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    /**
     * Gets recipes from webserver.
     * @return List<Recipe>
     */
    public static List<Recipe> getRecipes() {
        try {
            HttpUrl.Builder builder = HttpUrl.parse(BAKING_URL).newBuilder();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(builder.toString()).build();
            Response response = client.newCall(request).execute();
            return Parser.parserJsonToRecipes(response.body().string());
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }
        return null;
    }
}
