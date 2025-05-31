package com.example.projectfinalmobile.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.projectfinalmobile.Fragment.AktivitasFragment;
import com.example.projectfinalmobile.Fragment.BuatFragment;
import com.example.projectfinalmobile.Fragment.FavoriteFragment;
import com.example.projectfinalmobile.Fragment.HomeFragment;
import com.example.projectfinalmobile.Fragment.ProfilFragment;
import com.example.projectfinalmobile.ThemeHelper;
import com.example.projectfinalmobile.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Terapkan tema dari SharedPreferences
        if (ThemeHelper.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Set default fragment
        loadFragment(new HomeFragment());

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
