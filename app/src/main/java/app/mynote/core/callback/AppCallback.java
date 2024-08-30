package app.mynote.core.callback;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.net.ConnectException;

import app.mynote.LoginActivity;
import app.mynote.core.AppToast;
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
        } else if(response.raw().code() == 409) {
            AppToast.show(context,"Sync Conflict Occurred. Changes are not saved !!! Please refresh again", true);
        } else{
            Intent intent = new Intent(this.context, LoginActivity.class);
            this.context.startActivity(intent);
            onFailure(new HttpException(response));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if( t instanceof ConnectException){
            AppToast.show(context, "Failed to connect to sync server", true);
        }
        onFailure(t);
    }
}
