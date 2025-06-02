package com.example.projectfinalmobile.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfinalmobile.Database.DatabaseHelper;
import com.example.projectfinalmobile.Helper.AktivitasKuisHelper;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Model.PertanyaanModel;
import com.example.projectfinalmobile.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KerjakanActivity extends AppCompatActivity {
    private LinearLayout containerPertanyaan;
    private Button btnSimpan, btnBatal;

    private List<PertanyaanModel> pertanyaanList;
    private int indexSoal = 0;
    private String jawabanDipilih = null;
    private List<String> jawabanPengguna = new ArrayList<>();

    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kerjakan);

        containerPertanyaan = findViewById(R.id.container_pertanyaan);
        btnSimpan = findViewById(R.id.simpan);
        btnBatal = findViewById(R.id.batal);

        inflater = LayoutInflater.from(this);

        KuisModel kuis = getIntent().getParcelableExtra("data_kuis");

        if (kuis == null || kuis.getQuestions() == null || kuis.getQuestions().isEmpty()) {
            Toast.makeText(this, "Data kuis tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pertanyaanList = kuis.getQuestions();

        tampilkanPertanyaan(indexSoal);

        btnSimpan.setOnClickListener(v -> {
            if (jawabanDipilih == null) {
                Toast.makeText(this, "Silakan pilih salah satu jawaban terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            jawabanPengguna.add(jawabanDipilih);

            if (indexSoal < pertanyaanList.size() - 1) {
                indexSoal++;
                tampilkanPertanyaan(indexSoal);
                btnSimpan.setText(indexSoal == pertanyaanList.size() - 1 ? "Kirim" : "Selanjutnya");
            } else {
                int benar = 0;
                for (int i = 0; i < pertanyaanList.size(); i++) {
                    String kunciJawaban = pertanyaanList.get(i).getAnswer();
                    if (kunciJawaban != null && kunciJawaban.equalsIgnoreCase(jawabanPengguna.get(i))) {
                        benar++;
                    }
                }
                int skor = (int) ((double) benar / pertanyaanList.size() * 100);

                DatabaseHelper dbHelper = new DatabaseHelper(KerjakanActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                SharedPreferences sharedPreferences = this.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                int userId = sharedPreferences.getInt("user_id", -1);

                KuisHelper kuisHelper = new KuisHelper(this);
                long kuisId = kuisHelper.insertKuisLengkap(kuis, db);


                if (kuisId != -1) {
                    AktivitasKuisHelper aktivitasHelper = new AktivitasKuisHelper(this);
                    String tanggalSekarang = getTanggalHariIni();

                    aktivitasHelper.insertAktivitasKuis(userId, kuisId, skor, tanggalSekarang, db);
                } else {
                    Toast.makeText(this, "Kuis sudah pernah dikerjakan sebelumnya", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(KerjakanActivity.this, ScoreActivity.class);
                intent.putExtra("score", skor);
                startActivity(intent);
                finish();

            }
        });

        btnSimpan.setText(pertanyaanList.size() == 1 ? "Kirim" : "Selanjutnya");

        btnBatal.setOnClickListener(v -> finish());
    }

    private void tampilkanPertanyaan(int index) {
        containerPertanyaan.removeAllViews();

        View cardView = inflater.inflate(R.layout.card_pertanyaan, containerPertanyaan, false);

        PertanyaanModel soal = pertanyaanList.get(index);

        EditText edtPertanyaan = cardView.findViewById(R.id.pertanyaan);
        EditText opsi1 = cardView.findViewById(R.id.opsi1);
        EditText opsi2 = cardView.findViewById(R.id.opsi2);
        EditText opsi3 = cardView.findViewById(R.id.opsi3);
        EditText opsi4 = cardView.findViewById(R.id.opsi4);

        ImageView check1 = cardView.findViewById(R.id.accept_opsi1);
        ImageView check2 = cardView.findViewById(R.id.accept_opsi2);
        ImageView check3 = cardView.findViewById(R.id.accept_opsi3);
        ImageView check4 = cardView.findViewById(R.id.accept_opsi4);

        edtPertanyaan.setText(soal.getQuestion());
        List<String> opsi = soal.getOptions();

        if (opsi != null && opsi.size() >= 4) {
            opsi1.setText(opsi.get(0));
            opsi2.setText(opsi.get(1));
            opsi3.setText(opsi.get(2));
            opsi4.setText(opsi.get(3));
        }

        jawabanDipilih = null;
        resetCheckIcons(check1, check2, check3, check4);

        View.OnClickListener opsiClickListener = v -> {
            resetCheckIcons(check1, check2, check3, check4);
            ImageView clicked = (ImageView) v;
            clicked.setImageResource(R.drawable.accept);

            if (v == check1) jawabanDipilih = opsi1.getText().toString();
            else if (v == check2) jawabanDipilih = opsi2.getText().toString();
            else if (v == check3) jawabanDipilih = opsi3.getText().toString();
            else if (v == check4) jawabanDipilih = opsi4.getText().toString();
        };

        check1.setOnClickListener(opsiClickListener);
        check2.setOnClickListener(opsiClickListener);
        check3.setOnClickListener(opsiClickListener);
        check4.setOnClickListener(opsiClickListener);

        containerPertanyaan.addView(cardView);
    }

    private void resetCheckIcons(ImageView... checks) {
        for (ImageView check : checks) {
            check.setImageResource(R.drawable.check);
        }
    }

    private String getTanggalHariIni() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

}
