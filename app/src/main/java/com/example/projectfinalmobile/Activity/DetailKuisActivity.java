package com.example.projectfinalmobile.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class DetailKuisActivity extends AppCompatActivity {
    ImageView tvImage, btn_kembali, btn_favorit;
    Button btn_kerjakan;
    TextView tvJudul, tvKategori, tvTingkatKesulitan, tvTipe, tvjumlah_soal, belum_mengerjakan, score, text_score;

    private FavoritHelper favoritHelper;
    private AktivitasKuisHelper aktivitasKuisHelper;
    private KuisHelper kuisHelper;
    private int userId;
    private int kuisId;
    private boolean isFavorit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kuis);

        tvImage = findViewById(R.id.img_kuis);
        tvJudul = findViewById(R.id.judul_kuis);
        tvKategori = findViewById(R.id.kategori_kuis);
        tvTipe = findViewById(R.id.tipe_kuis);
        tvTingkatKesulitan = findViewById(R.id.tingkat_kesulitan);
        tvjumlah_soal = findViewById(R.id.jumlah_soal);
        btn_kembali = findViewById(R.id.btn_kembali);
        btn_favorit = findViewById(R.id.tambah_favorit);

        btn_kembali.setOnClickListener(v -> finish());

        kuisHelper = new KuisHelper(this);
        favoritHelper = new FavoritHelper(this);
        kuisHelper.open();
        favoritHelper.open();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        KuisModel kuis = getIntent().getParcelableExtra("data_kuis");

        if (kuis != null) {
            tvJudul.setText(kuis.getTitle());
            tvKategori.setText(kuis.getCategory());
            tvTipe.setText(kuis.getType());
            tvTingkatKesulitan.setText(kuis.getDifficulty());

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

            Picasso.get()
                    .load(kuis.getId_Image())
                    .placeholder(R.drawable.logout)
                    .error(R.drawable.accept)
                    .fit()
                    .centerCrop()
                    .into(tvImage);

            int jumlahSoal = kuis.getQuestions() != null ? kuis.getQuestions().size() : 0;
            tvjumlah_soal.setText(String.valueOf(jumlahSoal));

            if (!kuisHelper.isKuisExist(kuis.getTitle())) {
                SQLiteDatabase db = kuisHelper.getWritableDatabase();
                kuisHelper.insertKuisLengkap(kuis, db);
            }

            kuisId = kuisHelper.getIdByTitle(kuis.getTitle());
            isFavorit = favoritHelper.isFavorit(userId, kuisId);
            btn_favorit.setImageResource(isFavorit ? R.drawable.bookmark : R.drawable.bookmark_kosong);

            btn_favorit.setOnClickListener(v -> {
                if (userId == -1) {
                    Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isFavorit) {
                    favoritHelper.insertFavorit(userId, kuisId);
                    btn_favorit.setImageResource(R.drawable.bookmark);
                    Toast.makeText(this, "Berhasil ditambahkan ke favorit", Toast.LENGTH_SHORT).show();
                    isFavorit = true;
                } else {
                    Toast.makeText(this, "Kuis sudah ada di favorit", Toast.LENGTH_SHORT).show();
                }
            });

            btn_favorit.setOnClickListener(v -> {
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
                        btn_favorit.setImageResource(R.drawable.bookmark);
                        Toast.makeText(this, "Berhasil menambahkan ke favorit!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    builder.setTitle("Hapus dari Favorit");
                    builder.setMessage("Apakah Anda ingin menghapus dari favorite?");
                    builder.setPositiveButton("Ya", (dialog, which) -> {
                        favoritHelper.deleteFavorit(userId, kuisId);
                        btn_favorit.setImageResource(R.drawable.bookmark_kosong);
                        Toast.makeText(this, "Berhasil menghapus dari favorit", Toast.LENGTH_SHORT).show();

                    });

                }

                builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(ContextCompat.getColor(this, R.color.white))
                );
            });

            btn_kerjakan = findViewById(R.id.btn_kerjakan);

            aktivitasKuisHelper = new AktivitasKuisHelper(this);
            aktivitasKuisHelper.open();

            boolean sudahDikerjakan = aktivitasKuisHelper.isKuisSudahDikerjakan(userId, kuisId);

            text_score = findViewById(R.id.text_score);
            score = findViewById(R.id.score);
            belum_mengerjakan = findViewById(R.id.belum_mengerjakan);

            if (sudahDikerjakan) {
                btn_kerjakan.setText("Lihat Pembahasan");
                btn_kerjakan.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PembahasanActivity.class);
                    intent.putExtra("data_kuis", kuis);
                    startActivity(intent);
                });

                int score2 = aktivitasKuisHelper.getSkorByUserIdAndKuisId(userId, kuisId);
                score.setText(String.valueOf(score2));
                text_score.setVisibility(View.VISIBLE);
                score.setVisibility(View.VISIBLE); // pastikan terlihat

                belum_mengerjakan.setVisibility(View.GONE); // sembunyikan pesan
            } else {
                btn_kerjakan.setText("Kerjakan Kuis");
                btn_kerjakan.setOnClickListener(v -> {
                    if (kuis != null) {
                        Intent intent = new Intent(this, KerjakanActivity.class);
                        intent.putExtra("data_kuis", kuis);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Data kuis tidak tersedia", Toast.LENGTH_SHORT).show();
                    }
                });

                score.setVisibility(View.GONE);
                text_score.setVisibility(View.GONE);

                belum_mengerjakan.setVisibility(View.VISIBLE); // tampilkan pesan
            }






        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kuisHelper.close();
        favoritHelper.close();
    }
}
