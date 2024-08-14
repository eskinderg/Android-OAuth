package com.example.drawer.core;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.drawer.LoginActivity;

import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

public abstract class AppCallback<T> implements IAppCallback<T> {

    private final Context context;

    public AppCallback(Context context) {
        this.context = context;
    }

    public abstract void onResponse(T response);

    public abstract void onFailure(Throwable throwable);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onResponse(response.body());
        } else {
            Intent intent = new Intent(this.context, LoginActivity.class);
            this.context.startActivity(intent);
            onFailure(new HttpException(response));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onFailure(t);
        Toast.makeText(this.context, t.getMessage(), Toast.LENGTH_LONG).show();
    }
}
