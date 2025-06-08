package com.example.projectfinalmobile.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.projectfinalmobile.Fragment.AktivitasFragment;
import com.example.projectfinalmobile.Fragment.BuatFragment;
import com.example.projectfinalmobile.Fragment.FavoriteFragment;
import com.example.projectfinalmobile.Fragment.HomeFragment;
import com.example.projectfinalmobile.Fragment.ProfilFragment;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.ThemeHelper;
import com.example.projectfinalmobile.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId != -1) {
            boolean isDark = ThemeHelper.isDarkMode(this, String.valueOf(userId));
            if (isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        KuisModel kuisModel = getIntent().getParcelableExtra("data_kuis");

        if (getIntent() != null && getIntent().hasExtra("data_kuis")) {
            // Kirim data ke fragment
            Bundle bundle = new Bundle();
            bundle.putParcelable("data_kuis", kuisModel);

            BuatFragment buatFragment = new BuatFragment();
            buatFragment.setArguments(bundle);

            loadFragment(buatFragment);
            bottomNav.setSelectedItemId(R.id.tambah); // opsional: update bottom navigation
        } else {
            // Default: load HomeFragment
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.aktivitas) {
                selectedFragment = new AktivitasFragment();
            } else if (id == R.id.tambah) {
                selectedFragment = new BuatFragment();
            } else if (id == R.id.favorit) {
                selectedFragment = new FavoriteFragment();
            } else if (id == R.id.profil) {
                selectedFragment = new ProfilFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
