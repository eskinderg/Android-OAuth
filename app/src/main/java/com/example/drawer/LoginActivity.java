package com.example.drawer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drawer.auth.AccessToken;
import com.example.drawer.auth.AuthDataService;
import com.example.drawer.databinding.ActivityLoginBinding;
import com.example.drawer.service.RetroInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ProgressBar loadingProgressBar = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingProgressBar = binding.loading;

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        loginButton.setEnabled(true);
        final ProgressBar loadingProgressBar = binding.loading;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                getAccessToken(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
    }

    public void getAccessToken(String username, String password) {

        AuthDataService service = RetroInstance
                .getRetrofitInstance(Constants.KEYCLOAK_URL)
                .create(AuthDataService.class);

        Call<AccessToken> call = service.getAccessToken(Constants.CLIENT_ID, Constants.GRANT_TYPE, Constants.SCOPE, username, password);

        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (response.isSuccessful()) {
                    setAuthItems(response);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(intent);
                } else {
                    LoginActivity.this.loadingProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, response.message(), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                LoginActivity.this.loadingProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setAuthItems(Response<AccessToken> response) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("access_token", response.body().getAccessToken()).apply();
        sharedPreferences.edit().putString("refresh_token", response.body().getRefreshToken()).apply();
    }
}