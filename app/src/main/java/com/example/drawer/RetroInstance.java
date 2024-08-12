package com.example.drawer;


import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroInstance {

    static Retrofit retrofit;
    private static final String BASE_URL = Constants.BASE_API_URL;

    private static OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Log.w("app", chain.request().toString());
            Request newRequest  = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + Constants.ACCESS_TOKEN)
                    .build();
            return chain.proceed(newRequest);
        }
    }).build();

    public static Retrofit getRetrofitInstance() {
        if( retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

    }
}