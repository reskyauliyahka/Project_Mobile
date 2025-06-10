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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
                View view2 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog, null, false);

                TextView tvMessage = view2.findViewById(R.id.tvMessage);
                tvMessage.setText("Apakah Anda ingin mengubah tema aplikasi?");
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setView(view2)
                        .create();

                dialog.show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.white))
                    );
                }

                Button btnCancel = view2.findViewById(R.id.btnCancel);
                Button btnConfirm = view2.findViewById(R.id.btnConfirm);

                btnCancel.setOnClickListener(v1 -> dialog.dismiss());

                btnConfirm.setOnClickListener(v1 -> {
                    String userIdStr = String.valueOf(userId);
                    boolean isDark = ThemeHelper.isDarkMode(requireContext(), userIdStr);
                    ThemeHelper.setDarkMode(requireContext(), userIdStr, !isDark);
                    requireActivity().recreate();
                    dialog.dismiss();
                });
            }
        });


        logout.setOnClickListener(v -> {
            View view2 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog, null, false);

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setView(view2)
                    .create();

            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.white))
                );
            }

            Button btnCancel = view2.findViewById(R.id.btnCancel);
            Button btnConfirm = view2.findViewById(R.id.btnConfirm);

            btnCancel.setOnClickListener(v2 -> dialog.dismiss());

            btnConfirm.setOnClickListener(v2 -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            });


        });


        btn_editProfil.setOnClickListener(v -> showEditDialog());


        LineChart chartSkor = view.findViewById(R.id.chartSkor);

        AktivitasKuisHelper aktivitasKuisHelper = new AktivitasKuisHelper(getContext());
        aktivitasKuisHelper.open();

        List<Integer> historiSkor = aktivitasKuisHelper.getHistoriSkorByUserId(userId);

        if (historiSkor.isEmpty()) {
            chartSkor.setVisibility(View.GONE);
        } else {
            chartSkor.setVisibility(View.VISIBLE);

            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < historiSkor.size(); i++) {
                entries.add(new Entry(i + 1, historiSkor.get(i)));
            }

            Context context = requireContext();


            LineDataSet dataSet = new LineDataSet(entries, "Histori Skor");
            dataSet.setColor(ContextCompat.getColor(context, R.color.utama));
            dataSet.setValueTextColor(ContextCompat.getColor(context, R.color.black));
            dataSet.setCircleColor(ContextCompat.getColor(context, R.color.bg_susah));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);

            LineData lineData = new LineData(dataSet);
            chartSkor.setData(lineData);
            chartSkor.getDescription().setText("Grafik Skor Kuis");
            chartSkor.animateY(1000);
            chartSkor.getAxisLeft().setTextColor(ContextCompat.getColor(context, R.color.black));
            chartSkor.getAxisLeft().setTextSize(12f);
            chartSkor.getXAxis().setTextColor(ContextCompat.getColor(context, R.color.black));
            chartSkor.getAxisRight().setEnabled(false);
            chartSkor.invalidate();
        }



        rataSkor = view.findViewById(R.id.rataSkor);
        double rataRata = aktivitasKuisHelper.getRataRataSkorByUserId(userId);
        rataSkor.setText(String.format(Locale.getDefault(), "%.2f", rataRata));

        aktivitasKuisHelper.close();


        return view;
    }

    private void showEditDialog() {
        Context context = requireContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View dialogView = inflater.inflate(R.layout.edit_profil, null);

        EditText etUsername = dialogView.findViewById(R.id.username);
        EditText etEmail = dialogView.findViewById(R.id.email);
        ImageView imgProfile = dialogView.findViewById(R.id.profil);
        Button simpanPerubahan = dialogView.findViewById(R.id.update);

        etUsername.setText(username.getText().toString());
        etEmail.setText(email.getText().toString());
        if (imageProfileMain.getDrawable() != null) {
            imgProfile.setImageDrawable(imageProfileMain.getDrawable());
        }

        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            imgProfilInDialog = imgProfile;
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        final int userId = sharedPreferences.getInt("user_id", -1);

        simpanPerubahan.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(context, "Username dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == -1) {
                Toast.makeText(context, "User ID tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put("username", newUsername);
            values.put("email", newEmail);
            if (savedImagePath != null) {
                values.put("profile_picture", savedImagePath);
            }

            UserHelper userHelper = new UserHelper(context);
            userHelper.open();
            int rowsUpdated = userHelper.update(userId, values);
            userHelper.close();

            if (rowsUpdated > 0) {
                username.setText(newUsername);
                email.setText(newEmail);
                if (savedImagePath != null) {
                    imageProfileMain.setImageURI(Uri.fromFile(new File(savedImagePath)));
                }
                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
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
