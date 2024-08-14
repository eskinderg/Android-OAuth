package com.example.drawer.core;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface IAppCallback<T> extends Callback<T> {

    abstract void onResponse(Call<T> call, Response<T> response);

    abstract void onFailure(Call<T> call, Throwable throwable);

}
