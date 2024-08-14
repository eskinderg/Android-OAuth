package com.example.drawer.service;


import com.example.drawer.Constants;
import com.example.drawer.auth.AnnotationInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroInstance {

    public static final String BASE_API_URL = Constants.BASE_API_URL;
    static Retrofit retrofitApi;
    static Retrofit retrofitAuth;
    private static OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .addInterceptor(new AnnotationInterceptor())
            .build();


    public static Retrofit getRetrofitInstance() {
        if (retrofitApi == null) {
            retrofitApi = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitApi;

    }

    public static Retrofit getRetrofitInstance(String baseURL) {
        if (retrofitAuth == null) {
            retrofitAuth = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitAuth;
    }

}