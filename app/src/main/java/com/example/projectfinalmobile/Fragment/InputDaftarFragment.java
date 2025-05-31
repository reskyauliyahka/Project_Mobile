package com.example.projectfinalmobile.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Model.PertanyaanModel;
import com.example.projectfinalmobile.R;

import java.util.ArrayList;
import java.util.List;

public class InputDaftarFragment extends Fragment {

    private LinearLayout containerPertanyaan;
    private LayoutInflater inflater;
    private Button simpan;

    private final List<Integer> listJawabanBenar = new ArrayList<>();

    public InputDaftarFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_daftar, container, false);
        this.inflater = inflater;

        containerPertanyaan = view.findViewById(R.id.container_pertanyaan);
        View tombolTambah = view.findViewById(R.id.tambah_pertanyaan);
        simpan = view.findViewById(R.id.simpan);

        tambahPertanyaan();

        tombolTambah.setOnClickListener(v -> tambahPertanyaan());

        simpan.setOnClickListener(v -> simpanKeDatabase());

        return view;
    }

    private void tambahPertanyaan() {
        View card = inflater.inflate(R.layout.card_pertanyaan, containerPertanyaan, false);

        EditText[] opsi = {
                card.findViewById(R.id.opsi1),
                card.findViewById(R.id.opsi2),
                card.findViewById(R.id.opsi3),
                card.findViewById(R.id.opsi4)
        };

        ImageView[] acceptIcons = {
                card.findViewById(R.id.accept_opsi1),
                card.findViewById(R.id.accept_opsi2),
                card.findViewById(R.id.accept_opsi3),
                card.findViewById(R.id.accept_opsi4)
        };

        listJawabanBenar.add(-1);
        int currentIndex = listJawabanBenar.size() - 1;

        for (int i = 0; i < acceptIcons.length; i++) {
            final int index = i;
            acceptIcons[i].setOnClickListener(v -> {
                for (int j = 0; j < acceptIcons.length; j++) {
                    acceptIcons[j].setImageResource(R.drawable.check);
                }
                acceptIcons[index].setImageResource(R.drawable.accept);
                listJawabanBenar.set(currentIndex, index);
            });
        }

        containerPertanyaan.addView(card);
    }

    private void simpanKeDatabase() {
        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(getContext(), "Data kuis tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String judul = args.getString("judul");
        String kategori = args.getString("kategori");
        String tipe = args.getString("tipe");
        String tingkatKesulitan = args.getString("tingkat_kesulitan");
        String imgUrl = args.getString("img_url");

        // Ambil user ID dari SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PertanyaanModel> pertanyaanList = getDaftarPertanyaanLengkap();

        // VALIDASI: Pastikan ada pertanyaan
        if (pertanyaanList.isEmpty()) {
            Toast.makeText(getContext(), "Minimal ada satu pertanyaan", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDASI: Periksa tiap pertanyaan (soal, opsi, jawaban benar)
        for (int i = 0; i < pertanyaanList.size(); i++) {
            PertanyaanModel p = pertanyaanList.get(i);

            if (p.getQuestion() == null || p.getQuestion().isEmpty()) {
                Toast.makeText(getContext(), "Pertanyaan ke-" + (i + 1) + " tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hitung jumlah opsi yang valid
            List<String> opsi = p.getOptions();
            int jumlahOpsiValid = 0;
            for (String opsiText : opsi) {
                if (opsiText != null && !opsiText.isEmpty()) {
                    jumlahOpsiValid++;
                }
            }

// Validasi berdasarkan tipe kuis
            if (tipe.equalsIgnoreCase("pilihan ganda")) {
                if (jumlahOpsiValid != 4) {
                    Toast.makeText(getContext(), "Pertanyaan ke-" + (i + 1) + " harus memiliki 4 opsi jawaban untuk tipe 'pilihan ganda'", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (tipe.equalsIgnoreCase("boolean")) {
                if (jumlahOpsiValid != 2) {
                    Toast.makeText(getContext(), "Pertanyaan ke-" + (i + 1) + " harus memiliki 2 opsi jawaban untuk tipe 'boolean'", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(getContext(), "Tipe kuis tidak dikenali: " + tipe, Toast.LENGTH_SHORT).show();
                return;
            }


            if (p.getAnswer() == null || p.getAnswer().isEmpty()) {
                Toast.makeText(getContext(), "Jawaban benar untuk pertanyaan ke-" + (i + 1) + " belum dipilih", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        KuisModel kuis = new KuisModel();
        kuis.setTitle(judul);
        kuis.setCategory(kategori);
        kuis.setType(tipe);
        kuis.setDifficulty(tingkatKesulitan);
        kuis.setId_image(imgUrl);
        kuis.setUserId(String.valueOf(userId));
        kuis.setQuestions(pertanyaanList);

        KuisHelper dbHelper = new KuisHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = dbHelper.insertKuisLengkap(kuis, db);

        if (result != -1) {
            Toast.makeText(getContext(), "Kuis berhasil disimpan", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Gagal menyimpan kuis", Toast.LENGTH_SHORT).show();
        }
    }



    public List<PertanyaanModel> getDaftarPertanyaanLengkap() {
        List<PertanyaanModel> daftar = new ArrayList<>();

        for (int i = 0; i < containerPertanyaan.getChildCount(); i++) {
            View card = containerPertanyaan.getChildAt(i);

            EditText etPertanyaan = card.findViewById(R.id.pertanyaan);
            EditText[] opsi = {
                    card.findViewById(R.id.opsi1),
                    card.findViewById(R.id.opsi2),
                    card.findViewById(R.id.opsi3),
                    card.findViewById(R.id.opsi4)
            };

            String pertanyaanTeks = etPertanyaan.getText().toString().trim();
            List<String> opsiList = new ArrayList<>();
            for (EditText o : opsi) {
                opsiList.add(o.getText().toString().trim());
            }

            int jawabanIndex = listJawabanBenar.get(i);
            String jawabanBenar = (jawabanIndex >= 0 && jawabanIndex < 4) ? opsiList.get(jawabanIndex) : "";

            PertanyaanModel p = new PertanyaanModel();
            p.setQuestion(pertanyaanTeks);
            p.setOptions(opsiList);
            p.setAnswer(jawabanBenar);

            daftar.add(p);
        }

        return daftar;
    }

    public List<String> getSemuaJawabanBenar() {
        List<String> hasil = new ArrayList<>();

        for (int i = 0; i < containerPertanyaan.getChildCount(); i++) {
            View card = containerPertanyaan.getChildAt(i);

            EditText[] opsi = {
                    card.findViewById(R.id.opsi1),
                    card.findViewById(R.id.opsi2),
                    card.findViewById(R.id.opsi3),
                    card.findViewById(R.id.opsi4)
            };

            int jawabanIndex = listJawabanBenar.get(i);
            if (jawabanIndex >= 0 && jawabanIndex < 4) {
                hasil.add(opsi[jawabanIndex].getText().toString().trim());
            } else {
                hasil.add("");
            }
        }

        return hasil;
    }
}
