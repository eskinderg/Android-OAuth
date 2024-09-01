package app.mynote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.mynote.auth.AccessToken;
import app.mynote.auth.AuthConfig;
import app.mynote.auth.AuthDataService;
import app.mynote.auth.User;
import app.mynote.auth.UserManager;
import app.mynote.core.callback.AppCallback;
import app.mynote.service.RetroInstance;
import mynote.databinding.ActivityLoginBinding;
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
                userInfo();
            }

            @Override
            public void onFailure(Throwable throwable) {
                tryCheckAndAuthenticate();
            }
        });
    }

    private void setAuthItems(AccessToken response) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("access_token", response.getAccessToken()).apply();
        sharedPreferences.edit().putString("refresh_token", response.getRefreshToken()).apply();
    }

    private void userInfo() {

        AuthDataService service = RetroInstance.getRetrofitInstance(AuthConfig.KEYCLOAK_URL)
                .create(AuthDataService.class);

        Call<User> call = service.getUserInfo();

        call.enqueue(new AppCallback<User>(this) {
            @Override
            public void onResponse(User response) {
                setUser(response);
                LoginActivity.this.loadingProgressBar.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    private void setUser(User user) {
        AuthConfig.USER = user;
        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("sub", user.getId()).apply();
        sharedPreferences.edit().putString("given_name", user.getGivenName()).apply();
        sharedPreferences.edit().putString("email", user.getUserEmail()).apply();
    }

    private BiometricPrompt.PromptInfo buildBiometricPrompt() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login")
                .setSubtitle("Authentication")
                .setDescription("Authentication required to use MyNote")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK |
                                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build();

    }

    private void tryCheckAndAuthenticate() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricManager biometricManager = BiometricManager.from(this);
        BiometricPrompt.PromptInfo promptInfo = buildBiometricPrompt();
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                AuthConfig.USER = UserManager.getUser(LoginActivity.this);

                if (AuthConfig.USER != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(intent);
                } else {
                    LoginActivity.this.loadingProgressBar.setVisibility(View.INVISIBLE);
                    Toast("Unable to login user");
                }
            }

            @Override
            public void onAuthenticationFailed() {
                Toast("The FingerPrint was not recognized.Please Try Again!");
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    private void Toast(String message) {
        LoginActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}