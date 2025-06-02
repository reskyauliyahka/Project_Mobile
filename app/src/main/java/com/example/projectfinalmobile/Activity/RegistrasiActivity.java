package com.example.projectfinalmobile.Activity;


import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Helper.UserHelper;
import com.example.projectfinalmobile.R;

public class RegistrasiActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnDaftar;
    private UserHelper usersHelper;
    private TextView btn_login;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnDaftar = findViewById(R.id.btn_daftar);
        togglePassword = findViewById(R.id.hide);
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        isPasswordVisible = false;

        usersHelper = new UserHelper(this);
        usersHelper.open();

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class) ;
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

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            Toast.makeText(this, "Email harus menggunakan domain @gmail.com", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() <= 8) {
            Toast.makeText(this, "Password harus lebih dari 8 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (usersHelper.isUsernameExists(username)) {
            Toast.makeText(this, "Username sudah digunakan!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (usersHelper.isEmailExists(email)) {
            Toast.makeText(this, "Email sudah digunakan!", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Users.USERNAME, username);
        values.put(DatabaseContract.Users.EMAIL, email);
        values.put(DatabaseContract.Users.PASSWORD, password);

        long result = usersHelper.insert(values);
        if (result > 0) {
            Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegistrasiActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registrasi gagal!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        usersHelper.close();
    }
}
