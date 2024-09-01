package app.mynote.service;


import java.util.concurrent.TimeUnit;

import app.mynote.auth.AnnotationInterceptor;
import app.mynote.auth.AuthConfig;
import app.mynote.core.utils.TimeCalibrationInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroInstance {

    public static final String BASE_API_URL = AuthConfig.BASE_API_URL;
    private static final OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .addInterceptor(new AnnotationInterceptor())
            .addInterceptor(new TimeCalibrationInterceptor())
            .build();
    static Retrofit retrofitApi;
    static Retrofit retrofitAuth;

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
                    .client(client.newBuilder().connectTimeout(2, TimeUnit.SECONDS).build())
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitAuth;
    }

}