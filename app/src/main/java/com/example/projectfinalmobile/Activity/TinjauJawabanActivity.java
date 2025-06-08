package com.example.projectfinalmobile.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

        // Ambil data kuis dari Intent
        KuisModel kuis = getIntent().getParcelableExtra("data_kuis");
        if (kuis != null) {
            int kuisId = kuisHelper.getIdByTitle(kuis.getTitle());
            tampilkanSemuaSoalTanpaOpsi(kuis.getQuestions(), kuisId);
        }


    }

    private void tampilkanSemuaSoalTanpaOpsi(List<PertanyaanModel> listSoal, int kuisId) {
        Map<Integer, Map<String, Integer>> statistikJawaban = aktivitasKuisHelper.getStatistikJawabanByKuisId(kuisId);
        LayoutInflater inflater = LayoutInflater.from(this);
        containerPertanyaan.removeAllViews();

        int totalSeluruhJawaban = 0;

        for (int i = 0; i < listSoal.size(); i++) {
            PertanyaanModel soal = listSoal.get(i);
            View cardView = inflater.inflate(R.layout.card_tinjau_jawaban, containerPertanyaan, false);

            EditText edtPertanyaan = cardView.findViewById(R.id.pertanyaan);
            PieChart pieChart = cardView.findViewById(R.id.pie_chart_jawaban); // ✅ Perbaikan penting

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
            totalSeluruhJawaban += totalJawaban;


            // Siapkan data PieChart
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(jumlahBenar, "Benar"));
            entries.add(new PieEntry(jumlahSalah, "Salah"));

            PieDataSet dataSet = new PieDataSet(entries, "Hasil Jawaban");
            dataSet.setColors(
                    Color.parseColor("#009C9E"), // Benar
                    Color.parseColor("#87A75152")  // Salah
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
            pieChart.setEntryLabelColor(Color.BLACK);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(false);
            pieChart.getLegend().setEnabled(true);
            pieChart.invalidate();


            containerPertanyaan.addView(cardView);
        }

        if (totalJawabanText != null) {
            totalJawabanText.setText(String.valueOf(totalSeluruhJawaban));
        }
    }
}
