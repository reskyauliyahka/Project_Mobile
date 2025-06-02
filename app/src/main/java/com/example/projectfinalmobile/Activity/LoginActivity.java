package com.example.projectfinalmobile.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.projectfinalmobile.Helper.UserHelper;
import com.example.projectfinalmobile.R;
import com.example.projectfinalmobile.ThemeHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsernameEmail, etPassword;
    private Button btnLogin;
    private TextView daftar;
    private UserHelper usersHelper;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        etUsernameEmail = findViewById(R.id.usernameEmail);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        daftar = findViewById(R.id.daftar);
        togglePassword = findViewById(R.id.hide);

        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        usersHelper = new UserHelper(this);
        usersHelper.open();

        btnLogin.setOnClickListener(v -> checkLogin());

        daftar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrasiActivity.class);
            startActivity(intent);
        });

        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePassword.setImageResource(R.drawable.hide);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePassword.setImageResource(R.drawable.visible);
        }
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void checkLogin() {
        String input = etUsernameEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = usersHelper.checkUserLogin(input, password);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));

                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("user_id", userId);
                editor.putBoolean("is_logged_in", true);
                editor.apply();

                boolean isDark = ThemeHelper.isDarkMode(this, String.valueOf(userId));
                if (isDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Username/email atau password salah!", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } else {
            Toast.makeText(this, "Terjadi kesalahan saat login.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersHelper != null) {
            usersHelper.close();
        }
    }
}
