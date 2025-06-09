package com.example.projectfinalmobile.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.projectfinalmobile.Helper.AktivitasKuisHelper;
import com.example.projectfinalmobile.Helper.FavoritHelper;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DetailKuisActivity extends AppCompatActivity {
    private LinearLayout linearskor;
    private ImageView tvImage, btn_kembali, btn_favorit, btn_edit;
    private Button btn_kerjakan, btn_hapus;
    private TextView tvJudul, tvKategori, tvTingkatKesulitan, tvTipe, tvjumlah_soal;
    private TextView belum_mengerjakan, score, text_score;

    private FavoritHelper favoritHelper;
    private AktivitasKuisHelper aktivitasKuisHelper;
    private KuisHelper kuisHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private int kuisId;
    private boolean isFavorit;
    private KuisModel kuis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kuis);

        initView();
        initHelpers();

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        kuis = getIntent().getParcelableExtra("data_kuis");

        btn_kembali.setOnClickListener(v -> finish());

        btn_favorit.setOnClickListener(v -> toggleFavorit());

        btn_edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("data_kuis", kuis);
            startActivity(intent);
        });

        btn_hapus.setOnClickListener(v -> confirmHapusKuis());

        loadDetailKuis();
    }

    private void initView() {
        linearskor = findViewById(R.id.linearskor);
        tvImage = findViewById(R.id.img_kuis);
        tvJudul = findViewById(R.id.judul_kuis);
        tvKategori = findViewById(R.id.kategori_kuis);
        tvTipe = findViewById(R.id.tipe_kuis);
        tvTingkatKesulitan = findViewById(R.id.tingkat_kesulitan);
        tvjumlah_soal = findViewById(R.id.jumlah_soal);
        btn_kembali = findViewById(R.id.btn_kembali);
        btn_favorit = findViewById(R.id.tambah_favorit);
        btn_edit = findViewById(R.id.btn_edit);
        btn_hapus = findViewById(R.id.btn_hapus);
        btn_kerjakan = findViewById(R.id.btn_kerjakan);
        score = findViewById(R.id.score);
        text_score = findViewById(R.id.text_score);
        belum_mengerjakan = findViewById(R.id.belum_mengerjakan);
    }

    private void initHelpers() {
        kuisHelper = new KuisHelper(this);
        favoritHelper = new FavoritHelper(this);
        aktivitasKuisHelper = new AktivitasKuisHelper(this);
        kuisHelper.open();
        favoritHelper.open();
        aktivitasKuisHelper.open();
    }

    private void loadDetailKuis() {
        if (kuis == null) return;

        // Simpan jika belum ada di database lokal
        if (!kuisHelper.isKuisExist(kuis.getTitle())) {
            SQLiteDatabase db = kuisHelper.getWritableDatabase();
            kuisHelper.insertKuisLengkap(kuis, db);
        }

        kuisId = kuisHelper.getIdByTitle(kuis.getTitle());
        String status = kuisHelper.getStatusById(kuisId);
        kuis.setStatus(status);

        isFavorit = favoritHelper.isFavorit(userId, kuisId);

        tvJudul.setText(kuis.getTitle());
        tvKategori.setText(kuis.getCategory());
        tvTipe.setText(kuis.getType());
        tvTingkatKesulitan.setText(kuis.getDifficulty());

        int jumlahSoal = kuis.getQuestions() != null ? kuis.getQuestions().size() : 0;
        tvjumlah_soal.setText(String.valueOf(jumlahSoal));

        btn_favorit.setImageResource(isFavorit ? R.drawable.bookmark : R.drawable.bookmark_kosong);

        // Set gambar
        Picasso.get()
                .load(kuis.getId_Image())
                .placeholder(R.drawable.logout)
                .error(R.drawable.accept)
                .fit()
                .centerCrop()
                .into(tvImage);

        // Warna kesulitan
        switch (kuis.getDifficulty()) {
            case "Mudah":
                tvTingkatKesulitan.setTextColor(ContextCompat.getColor(this, R.color.utama));
                tvTingkatKesulitan.setBackgroundResource(R.drawable.rounded_klik);
                break;
            case "Sedang":
                tvTingkatKesulitan.setTextColor(ContextCompat.getColor(this, R.color.teks_sedang));
                tvTingkatKesulitan.setBackgroundResource(R.drawable.bg_sedang);
                break;
            case "Sulit":
                tvTingkatKesulitan.setTextColor(ContextCompat.getColor(this, R.color.teks_susah));
                tvTingkatKesulitan.setBackgroundResource(R.drawable.bg_susah);
                break;
        }

        updateUIBerdasarkanStatus();
    }

    private void updateUIBerdasarkanStatus() {
        boolean isOwnedByUser = false;

        List<KuisModel> userKuisList = kuisHelper.getKuisByUserId(String.valueOf(userId));
        for (KuisModel userKuis : userKuisList) {
            if (userKuis.getId() == kuisId) {
                isOwnedByUser = true;
                break;
            }
        }

        if (isOwnedByUser) {
            btn_edit.setVisibility(View.VISIBLE);
            btn_hapus.setVisibility(View.VISIBLE);
            linearskor.setVisibility(View.GONE);
            btn_kerjakan.setText("Tinjau Responden");
            btn_kerjakan.setOnClickListener(v -> {
                Intent intent = new Intent(this, TinjauJawabanActivity.class);
                intent.putExtra("data_kuis", kuis);
                startActivity(intent);
            });

            btn_favorit.setImageResource(
                    kuis.getStatus().equalsIgnoreCase("tutup") ? R.drawable.lock : R.drawable.unlock
            );

            btn_favorit.setOnClickListener(v -> {
                String pesan = kuis.getStatus().equalsIgnoreCase("tutup") ?
                        "Apakah Anda ingin membuka kuis ini?" :
                        "Apakah Anda ingin menutup kuis ini?";

                String statusBaru = kuis.getStatus().equalsIgnoreCase("tutup") ? "buka" : "tutup";

                new AlertDialog.Builder(this)
                        .setTitle("Ubah Status Kuis")
                        .setMessage(pesan)
                        .setPositiveButton("Ya", (dialog, which) -> {
                            boolean sukses = kuisHelper.updateStatusKuis(kuisId, statusBaru);
                            if (sukses) {
                                kuis.setStatus(statusBaru);
                                Toast.makeText(this, "Status berhasil diperbarui ke " + statusBaru, Toast.LENGTH_SHORT).show();
                                btn_favorit.setImageResource(
                                        statusBaru.equals("tutup") ? R.drawable.lock : R.drawable.unlock
                                );
                            } else {
                                Toast.makeText(this, "Gagal memperbarui status", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            });

        } else {
            boolean sudahDikerjakan = aktivitasKuisHelper.isKuisSudahDikerjakan(userId, kuisId);
            if (sudahDikerjakan) {
                int skor = aktivitasKuisHelper.getSkorByUserIdAndKuisId(userId, kuisId);
                score.setText(String.valueOf(skor));
                text_score.setVisibility(View.VISIBLE);
                score.setVisibility(View.VISIBLE);
                belum_mengerjakan.setVisibility(View.GONE);
                btn_kerjakan.setText("Lihat Pembahasan");

                List<String> jawabanUser = aktivitasKuisHelper.getJawabanUser(userId, kuisId);
                kuis.setJawabanUser(jawabanUser);

                btn_kerjakan.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PembahasanActivity.class);
                    intent.putExtra("data_kuis", kuis);
                    intent.putExtra("score", skor);
                    startActivity(intent);
                });
            } else {
                score.setVisibility(View.GONE);
                text_score.setVisibility(View.GONE);
                belum_mengerjakan.setVisibility(View.VISIBLE);
                btn_kerjakan.setText("Kerjakan Kuis");
                btn_kerjakan.setOnClickListener(v -> {
                    if (kuis.getStatus() != null && kuis.getStatus().equalsIgnoreCase("tutup")) {
                        Toast.makeText(this, "Kuis ini telah ditutup dan tidak dapat dikerjakan.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(this, KerjakanActivity.class);
                        intent.putExtra("data_kuis", kuis);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void toggleFavorit() {
        if (userId == -1) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        if (!isFavorit) {
            builder.setTitle("Tambah ke Favorit");
            builder.setMessage("Apakah Anda ingin menambahkan ke favorite?");
            builder.setPositiveButton("Ya", (dialog, which) -> {
                favoritHelper.insertFavorit(userId, kuisId);
                isFavorit = true;
                btn_favorit.setImageResource(R.drawable.bookmark);
                Toast.makeText(this, "Berhasil menambahkan ke favorit!", Toast.LENGTH_SHORT).show();
            });
        } else {
            builder.setTitle("Hapus dari Favorit");
            builder.setMessage("Apakah Anda ingin menghapus dari favorite?");
            builder.setPositiveButton("Ya", (dialog, which) -> {
                favoritHelper.deleteFavorit(userId, kuisId);
                isFavorit = false;
                btn_favorit.setImageResource(R.drawable.bookmark_kosong);
                Toast.makeText(this, "Berhasil menghapus dari favorit", Toast.LENGTH_SHORT).show();
            });
        }

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.white)));
    }

    private void confirmHapusKuis() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hapus Kuis");
        builder.setMessage("Apakah Anda yakin ingin menghapus kuis ini?");
        builder.setPositiveButton("Ya", (dialog, which) -> {
            kuisHelper.deleteFullKuisById(kuisId);
            Toast.makeText(this, "Kuis berhasil dihapus", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDetailKuis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kuisHelper.close();
        favoritHelper.close();
        aktivitasKuisHelper.close();
    }
}
