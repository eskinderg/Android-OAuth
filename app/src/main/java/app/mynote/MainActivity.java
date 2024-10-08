package app.mynote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import app.mynote.auth.AuthConfig;
import app.mynote.auth.AuthDataService;
import app.mynote.auth.User;
import app.mynote.core.MyNote;
import app.mynote.core.callback.AppCallback;
import app.mynote.core.db.NoteSyncAdapter;
import app.mynote.core.db.auth.AccountGeneral;
import app.mynote.service.RetroInstance;
import mynote.R;
import mynote.databinding.ActivityMainBinding;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountGeneral.createSyncAccount(this);

        NoteSyncAdapter.performSync();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.userName);
        TextView navEmail = headerView.findViewById(R.id.userEmail);

        navUsername.setText(AuthConfig.USER.getGivenName());
        navEmail.setText(AuthConfig.USER.getUserEmail());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notes, R.id.nav_events, R.id.nav_logout, R.id.nav_note, R.id.nav_pin, R.id.nav_archived_notes, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                AuthDataService service = RetroInstance.getRetrofitInstance(AuthConfig.KEYCLOAK_URL)
                        .create(AuthDataService.class);
                Call<ResponseBody> call = service.logout(AuthConfig.CLIENT_ID, MyNote.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("refresh_token", ""));
                call.enqueue(new AppCallback<ResponseBody>(MainActivity.this) {
                    @Override
                    public void onResponse(ResponseBody response) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                    }
                });

                return true;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}