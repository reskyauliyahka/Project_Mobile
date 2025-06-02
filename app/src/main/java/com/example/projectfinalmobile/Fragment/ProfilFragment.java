package com.example.projectfinalmobile.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.projectfinalmobile.Activity.LoginActivity;
import com.example.projectfinalmobile.Helper.UserHelper;
import com.example.projectfinalmobile.R;
import com.example.projectfinalmobile.ThemeHelper;

public class ProfilFragment extends Fragment {
    private TextView username, email;

    private static final String TAG = "ProfilFragment";

    public ProfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ProfilFragment dimuat");

        View view = inflater.inflate(R.layout.fragment_profil, container, false);
        LinearLayout temaLayout = view.findViewById(R.id.thema);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        temaLayout.setOnClickListener(v -> {
            if (userId != -1) {
                String userIdStr = String.valueOf(userId);
                boolean isDark = ThemeHelper.isDarkMode(requireContext(), userIdStr);
                Log.d(TAG, "Tema saat ini: " + (isDark ? "Dark" : "Light"));

                ThemeHelper.setDarkMode(requireContext(), userIdStr, !isDark);
                Log.d(TAG, "Mengubah tema menjadi: " + (!isDark ? "Dark" : "Light"));

                requireActivity().recreate(); // Terapkan perubahan tema
            } else {
                Log.e(TAG, "Gagal toggle tema: user_id tidak ditemukan di SharedPreferences");
            }
        });

        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);

        if (userId != -1) {
            UserHelper userHelper = new UserHelper(requireContext());
            userHelper.open();

            Cursor cursor = userHelper.getUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String fetchedUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String fetchedEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));

                username.setText(fetchedUsername);
                email.setText(fetchedEmail);

                cursor.close();
            } else {
                Log.e(TAG, "Gagal mengambil data user dari database");
            }

            userHelper.close();
        } else {
            Log.e(TAG, "User ID tidak ditemukan di SharedPreferences");
        }

        LinearLayout logout = view.findViewById(R.id.keluar);
        logout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
