package com.example.drawer.core.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface IAppCallback<T> extends Callback<T> {

    void onResponse(Call<T> call, Response<T> response);

    void onFailure(Call<T> call, Throwable throwable);

}
