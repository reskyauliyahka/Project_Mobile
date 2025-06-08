package com.example.projectfinalmobile.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectfinalmobile.Activity.LoginActivity;
import com.example.projectfinalmobile.Helper.AktivitasKuisHelper;
import com.example.projectfinalmobile.Helper.UserHelper;
import com.example.projectfinalmobile.R;
import com.example.projectfinalmobile.ThemeHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class ProfilFragment extends Fragment {

    private TextView username, email, rataSkor;
    private ImageView btn_editProfil, imageProfileMain;
    private static final String TAG = "ProfilFragment";
    private static final int REQUEST_PICK_IMAGE = 101;

    private ImageView imgProfilInDialog;
    private Uri selectedImageUri = null;
    private String savedImagePath = null;

    public ProfilFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ProfilFragment dimuat");

        View view = inflater.inflate(R.layout.fragment_profil, container, false);
        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        imageProfileMain = view.findViewById(R.id.fotoProfil);
        btn_editProfil = view.findViewById(R.id.btn_editProfil);
        LinearLayout temaLayout = view.findViewById(R.id.thema);
        LinearLayout logout = view.findViewById(R.id.keluar);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId != -1) {
            UserHelper userHelper = new UserHelper(requireContext());
            userHelper.open();

            Cursor cursor = userHelper.getUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String fetchedUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String fetchedEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String profilePicPath = cursor.getString(cursor.getColumnIndexOrThrow("profile_picture"));

                username.setText(fetchedUsername);
                email.setText(fetchedEmail);

                if (profilePicPath != null && !profilePicPath.isEmpty()) {
                    imageProfileMain.setImageURI(Uri.fromFile(new File(profilePicPath)));
                }

                cursor.close();
            }
            userHelper.close();
        }

        temaLayout.setOnClickListener(v -> {
            if (userId != -1) {
                String userIdStr = String.valueOf(userId);
                boolean isDark = ThemeHelper.isDarkMode(requireContext(), userIdStr);
                ThemeHelper.setDarkMode(requireContext(), userIdStr, !isDark);
                requireActivity().recreate();
            }
        });

        logout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        btn_editProfil.setOnClickListener(v -> showEditDialog());

        rataSkor = view.findViewById(R.id.rataSkor);

        AktivitasKuisHelper aktivitasKuisHelper = new AktivitasKuisHelper(getContext());
        aktivitasKuisHelper.open();
        double rataRata = aktivitasKuisHelper.getRataRataSkorByUserId(userId);
        if (rataRata == 0) {
            rataSkor.setText("-");
        } else {
            rataSkor.setText(String.format(Locale.getDefault(), "%.2f", rataRata));
        }
        aktivitasKuisHelper.close();


        return view;
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.edit_profil, null);

        EditText etUsername = dialogView.findViewById(R.id.username);
        EditText etEmail = dialogView.findViewById(R.id.email);
        ImageView imgProfile = dialogView.findViewById(R.id.profil);
        Button simpanPerubahan = dialogView.findViewById(R.id.update);

        etUsername.setText(username.getText().toString());
        etEmail.setText(email.getText().toString());
        imgProfile.setImageDrawable(imageProfileMain.getDrawable());


        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            imgProfilInDialog = imgProfile;
        });


        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.6), // Misalnya 80% lebar layar
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        final int userId = sharedPreferences.getInt("user_id", -1);

        simpanPerubahan.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(getContext(), "Username dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == -1) {
                Toast.makeText(getContext(), "User ID tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put("username", newUsername);
            values.put("email", newEmail);
            if (savedImagePath != null) {
                values.put("profile_picture", savedImagePath);
            }

            UserHelper userHelper = new UserHelper(requireContext());
            userHelper.open();
            int rowsUpdated = userHelper.update(userId, values);
            userHelper.close();

            if (rowsUpdated > 0) {
                username.setText(newUsername);
                email.setText(newEmail);
                if (savedImagePath != null) {
                    imageProfileMain.setImageURI(Uri.fromFile(new File(savedImagePath)));
                }
                Toast.makeText(getContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), selectedImageUri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                    }

                    File imageFile = new File(requireContext().getFilesDir(), "profile_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();

                    savedImagePath = imageFile.getAbsolutePath();
                    if (imgProfilInDialog != null) {
                        imgProfilInDialog.setImageURI(Uri.fromFile(imageFile));
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Gagal menyimpan gambar", e);
                    Toast.makeText(getContext(), "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
