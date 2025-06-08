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
    private KuisModel kuisLama = null;


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

        Bundle args = getArguments();
        if (args != null && args.containsKey("data_kuis")) {
            kuisLama = args.getParcelable("data_kuis");
            if (kuisLama != null && kuisLama.getQuestions() != null) {
                isiPertanyaanDariData(kuisLama);
                simpan.setText("Perbarui");
            }
        }

        simpan.setOnClickListener(v -> simpanKeDatabase());

        return view;
    }


    private void tambahPertanyaan() {
        View card = inflater.inflate(R.layout.card_pertanyaan, containerPertanyaan, false);


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

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PertanyaanModel> pertanyaanList = getDaftarPertanyaanLengkap();
        if (pertanyaanList.isEmpty()) {
            Toast.makeText(getContext(), "Minimal ada satu pertanyaan", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < pertanyaanList.size(); i++) {
            PertanyaanModel p = pertanyaanList.get(i);

            if (p.getQuestion() == null || p.getQuestion().isEmpty()) {
                Toast.makeText(getContext(), "Pertanyaan ke-" + (i + 1) + " tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> opsi = p.getOptions();
            int jumlahOpsiValid = 0;
            for (String opsiText : opsi) {
                if (opsiText != null && !opsiText.isEmpty()) {
                    jumlahOpsiValid++;
                }
            }

            String tipe = kuisLama != null ? kuisLama.getType() : args.getString("tipe");

            if (tipe.equalsIgnoreCase("pilihan ganda") && jumlahOpsiValid != 4) {
                Toast.makeText(getContext(), "Pertanyaan ke-" + (i + 1) + " harus punya 4 opsi", Toast.LENGTH_SHORT).show();
                return;
            } else if (tipe.equalsIgnoreCase("boolean") && jumlahOpsiValid != 2) {
                Toast.makeText(getContext(), "Pertanyaan ke-" + (i + 1) + " harus punya 2 opsi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (p.getAnswer() == null || p.getAnswer().isEmpty()) {
                Toast.makeText(getContext(), "Jawaban benar belum dipilih untuk pertanyaan ke-" + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        KuisHelper dbHelper = new KuisHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int kuis_id = dbHelper.getIdByTitle(kuisLama.getTitle());

        if (kuisLama != null && kuis_id > 0) {
            // ======= MODE UPDATE =======
            kuisLama.setQuestions(pertanyaanList);
            kuisLama.setId(kuis_id);
            kuisLama.setUserId(String.valueOf(userId));
            boolean updated = dbHelper.updateKuisLengkap(kuisLama, db);
            if (updated) {
                Toast.makeText(getContext(), "Kuis berhasil diperbarui", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Gagal memperbarui kuis", Toast.LENGTH_SHORT).show();
            }

        } else {
            // ======= MODE BUAT BARU =======
            String judul = args.getString("judul");
            String kategori = args.getString("kategori");
            String tipe = args.getString("tipe");
            String tingkatKesulitan = args.getString("tingkat_kesulitan");
            String imgUrl = args.getString("img_url");

            KuisModel kuis = new KuisModel();
            kuis.setTitle(judul);
            kuis.setCategory(kategori);
            kuis.setType(tipe);
            kuis.setDifficulty(tingkatKesulitan);
            kuis.setId_image(imgUrl);
            kuis.setUserId(String.valueOf(userId));
            kuis.setQuestions(pertanyaanList);

            long result = dbHelper.insertKuisLengkap(kuis, db);
            if (result != -1) {
                Toast.makeText(getContext(), "Kuis berhasil disimpan", Toast.LENGTH_SHORT).show();
                clearInput();
            } else {
                Toast.makeText(getContext(), "Gagal menyimpan kuis", Toast.LENGTH_SHORT).show();
            }
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

    private void clearInput() {
        containerPertanyaan.removeAllViews();
        listJawabanBenar.clear();
        tambahPertanyaan(); // Tambahkan satu pertanyaan awal kembali
    }

    private void isiPertanyaanDariData(KuisModel kuis) {
        containerPertanyaan.removeAllViews();
        listJawabanBenar.clear();

        List<PertanyaanModel> daftarPertanyaan = kuis.getQuestions();
        if (daftarPertanyaan == null) return;

        for (PertanyaanModel p : daftarPertanyaan) {
            View card = inflater.inflate(R.layout.card_pertanyaan, containerPertanyaan, false);

            EditText etPertanyaan = card.findViewById(R.id.pertanyaan);
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

            etPertanyaan.setText(p.getQuestion());
            List<String> opsiList = p.getOptions();

            for (int i = 0; i < opsi.length; i++) {
                if (i < opsiList.size()) {
                    opsi[i].setText(opsiList.get(i));
                }
            }

            int indexJawaban = -1;
            for (int i = 0; i < opsiList.size(); i++) {
                if (opsiList.get(i).equals(p.getAnswer())) {
                    indexJawaban = i;
                    break;
                }
            }

            listJawabanBenar.add(indexJawaban);
            int currentIndex = listJawabanBenar.size() - 1;

            for (int i = 0; i < acceptIcons.length; i++) {
                final int idx = i;
                acceptIcons[i].setOnClickListener(v -> {
                    for (int j = 0; j < acceptIcons.length; j++) {
                        acceptIcons[j].setImageResource(R.drawable.check);
                    }
                    acceptIcons[idx].setImageResource(R.drawable.accept);
                    listJawabanBenar.set(currentIndex, idx);
                });

                // Jika jawaban saat ini benar, tampilkan tanda centang
                if (i == indexJawaban) {
                    acceptIcons[i].setImageResource(R.drawable.accept);
                } else {
                    acceptIcons[i].setImageResource(R.drawable.check);
                }
            }

            containerPertanyaan.addView(card);
        }
    }



}
