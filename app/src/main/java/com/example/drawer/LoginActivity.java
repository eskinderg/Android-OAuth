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
import com.example.drawer.auth.AuthConfig;
import com.example.drawer.auth.AuthDataService;
import com.example.drawer.core.callback.AppCallback;
import com.example.drawer.databinding.ActivityLoginBinding;
import com.example.drawer.service.RetroInstance;

import retrofit2.Call;

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
                .getRetrofitInstance(AuthConfig.KEYCLOAK_URL)
                .create(AuthDataService.class);

        Call<AccessToken> call = service.getAccessToken(AuthConfig.CLIENT_ID, AuthConfig.GRANT_TYPE, AuthConfig.SCOPE, username, password);

        call.enqueue(new AppCallback<AccessToken>(LoginActivity.this) {
            @Override
            public void onResponse(AccessToken response) {
                setAuthItems(response);
                LoginActivity.this.loadingProgressBar.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
            }

            @Override
            public void onFailure(Throwable throwable) {
                LoginActivity.this.loadingProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, throwable.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setAuthItems(AccessToken response) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("access_token", response.getAccessToken()).apply();
        sharedPreferences.edit().putString("refresh_token", response.getRefreshToken()).apply();
    }
}