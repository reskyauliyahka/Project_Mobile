package com.example.projectfinalmobile.Activity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.projectfinalmobile.Helper.AktivitasKuisHelper;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Model.PertanyaanModel;
import com.example.projectfinalmobile.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TinjauJawabanActivity extends AppCompatActivity {

    private LinearLayout containerPertanyaan;
    private AktivitasKuisHelper aktivitasKuisHelper;
    private KuisHelper kuisHelper;
    private TextView totalJawabanText;
    private Button lihatUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tinjau_jawaban);

        totalJawabanText = findViewById(R.id.totalJawaban);

        containerPertanyaan = findViewById(R.id.container_pertanyaan);
        aktivitasKuisHelper = new AktivitasKuisHelper(this);
        kuisHelper = new KuisHelper(this);

        aktivitasKuisHelper.open();
        kuisHelper.open();

        KuisModel kuis = getIntent().getParcelableExtra("data_kuis");
        int kuisId = kuisHelper.getIdByTitle(kuis.getTitle());
        if (kuis != null) {
            tampilkanSemuaSoalTanpaOpsi(kuis.getQuestions(), kuisId);
        }

        lihatUser = findViewById(R.id.lihatUser);
        lihatUser.setOnClickListener(v -> {
            tampilkanDaftarUserYangMengerjakan(kuisId);

        });

        Button btn_kembali = findViewById(R.id.batal);
        btn_kembali.setOnClickListener(v -> finish());

        List<Map<String, Object>> daftarUser2 = aktivitasKuisHelper.getUserYangMengerjakanKuis(kuisId);
        int totalJawabanKuis = daftarUser2.size();

        totalJawabanText.setText(String.valueOf(totalJawabanKuis));

    }

    private void tampilkanSemuaSoalTanpaOpsi(List<PertanyaanModel> listSoal, int kuisId) {
        Map<Integer, Map<String, Integer>> statistikJawaban = aktivitasKuisHelper.getStatistikJawabanByKuisId(kuisId);
        LayoutInflater inflater = LayoutInflater.from(this);
        containerPertanyaan.removeAllViews();


        for (int i = 0; i < listSoal.size(); i++) {
            PertanyaanModel soal = listSoal.get(i);
            View cardView = inflater.inflate(R.layout.card_tinjau_jawaban, containerPertanyaan, false);

            EditText edtPertanyaan = cardView.findViewById(R.id.pertanyaan);
            PieChart pieChart = cardView.findViewById(R.id.pie_chart_jawaban);

            edtPertanyaan.setText(soal.getQuestion());
            edtPertanyaan.setEnabled(false);

            Map<String, Integer> distribusiJawaban = statistikJawaban.getOrDefault(i, new HashMap<>());
            String jawabanBenar = soal.getAnswer();

            int jumlahBenar = distribusiJawaban.getOrDefault(jawabanBenar, 0);
            int totalJawaban = 0;
            for (int jumlah : distribusiJawaban.values()) {
                totalJawaban += jumlah;
            }
            int jumlahSalah = totalJawaban - jumlahBenar;

            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(jumlahBenar, "Benar"));
            entries.add(new PieEntry(jumlahSalah, "Salah"));

            PieDataSet dataSet = new PieDataSet(entries, "Hasil Jawaban");
            dataSet.setColors(
                    Color.parseColor("#009C9E"),
                    Color.parseColor("#87A75152")
            );
            dataSet.setValueTextSize(14f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setDrawValues(true);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });

            pieChart.setDrawEntryLabels(false);
            pieChart.setData(data);
            pieChart.setUsePercentValues(false);
            pieChart.setEntryLabelTextSize(14f);
            int warnaHitam = ContextCompat.getColor(this, R.color.black);
            pieChart.setEntryLabelColor(warnaHitam);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(false);
            pieChart.getLegend().setEnabled(true);
            pieChart.invalidate();
            pieChart.getLegend().setTextColor(warnaHitam);

            containerPertanyaan.addView(cardView);
        }

    }

    private void tampilkanDaftarUserYangMengerjakan(int kuisId) {
        List<Map<String, Object>> daftarUser = aktivitasKuisHelper.getUserYangMengerjakanKuis(kuisId);
        LayoutInflater inflater = LayoutInflater.from(this);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        for (Map<String, Object> userData : daftarUser) {
            View userCard = inflater.inflate(R.layout.card_user, null);

            TextView usernameView = userCard.findViewById(R.id.username);
            TextView skorView = userCard.findViewById(R.id.skor);
            ImageView fotoProfilView = userCard.findViewById(R.id.foto_profil);

            String usernameStr = (String) userData.get("username");
            int skorInt = (int) userData.get("skor");
            String fotoProfilStr = (String) userData.get("foto_profil");

            usernameView.setText(usernameStr);
            skorView.setText(String.valueOf(skorInt));

            if (fotoProfilStr != null && !fotoProfilStr.isEmpty()) {
                try {
                    Uri uri = Uri.parse(fotoProfilStr);
                    fotoProfilView.setImageURI(uri);
                } catch (Exception e) {
                    fotoProfilView.setImageResource(R.drawable.userprofil);
                }
            } else {
                fotoProfilView.setImageResource(R.drawable.userprofil);
            }

            container.addView(userCard);
        }

        TextView title = new TextView(this);
        title.setText("User yang telah mengerjakan kuis");
        title.setPadding(20, 40, 40, 40);
        title.setTextSize(16f);
        title.setTextColor(ContextCompat.getColor(this, R.color.utama));
        title.setTypeface(ResourcesCompat.getFont(this, R.font.nexaheavy));
        title.setGravity(Gravity.CENTER);


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(title)
                .setView(container)
                .setPositiveButton("Tutup", null)
                .show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(this, R.color.white))
            );
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.utama));
    }

}
