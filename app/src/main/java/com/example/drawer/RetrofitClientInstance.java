package com.example.drawer;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {

    static Retrofit retrofit;
    private static final String BASE_URL = Constants.KEYCLOAK_URL;

    public static Retrofit getRetrofitInstance() {
        if( retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

    }
}
