package com.example.projectfinalmobile.Activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Model.PertanyaanModel;
import com.example.projectfinalmobile.Networking.AIResponse;
import com.example.projectfinalmobile.Networking.ApiService;
import com.example.projectfinalmobile.Networking.OpenAIApiClient;
import com.example.projectfinalmobile.R;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PembahasanActivity extends AppCompatActivity {

    private LinearLayout containerPembahasan;
    private LayoutInflater inflater;
    private ApiService apiService;
    private KuisModel kuis;  // Jadikan field agar bisa dipakai di method lain

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembahasan);

        containerPembahasan = findViewById(R.id.container_pembahasan);
        inflater = LayoutInflater.from(this);
        apiService = OpenAIApiClient.getGeminiService();
        TextView skor = findViewById(R.id.skor);

        int score = getIntent().getIntExtra("score", 0);
        skor.setText(String.valueOf(score));

        kuis = getIntent().getParcelableExtra("data_kuis");  // Simpan ke field

        if (kuis != null && kuis.getQuestions() != null) {
            tampilkanSemuaSoal(kuis.getQuestions());
        }

        findViewById(R.id.btn_kembali).setOnClickListener(v -> finish());
    }

    private void tampilkanSemuaSoal(List<PertanyaanModel> listSoal) {
        List<String> jawabanUser = kuis != null ? kuis.getJawabanUser() : null;  // Aman jika kuis null

        for (int i = 0; i < listSoal.size(); i++) {
            PertanyaanModel soal = listSoal.get(i);
            View cardView = inflater.inflate(R.layout.card_pembahasan, containerPembahasan, false);
            TextView teks_opsi3 = cardView.findViewById(R.id.tekspilihan3);
            TextView teks_opsi4 = cardView.findViewById(R.id.tekspilihan4);

            EditText edtPertanyaan = cardView.findViewById(R.id.pertanyaan);
            EditText opsi1 = cardView.findViewById(R.id.opsi1);
            EditText opsi2 = cardView.findViewById(R.id.opsi2);
            EditText opsi3 = cardView.findViewById(R.id.opsi3);
            EditText opsi4 = cardView.findViewById(R.id.opsi4);

            ImageView check1 = cardView.findViewById(R.id.accept_opsi1);
            ImageView check2 = cardView.findViewById(R.id.accept_opsi2);
            ImageView check3 = cardView.findViewById(R.id.accept_opsi3);
            ImageView check4 = cardView.findViewById(R.id.accept_opsi4);

            TextView pembahasan = cardView.findViewById(R.id.pembahasan);

            // Disable input supaya tidak bisa diedit
            edtPertanyaan.setEnabled(false);
            opsi1.setEnabled(false);
            opsi2.setEnabled(false);
            opsi3.setEnabled(false);
            opsi4.setEnabled(false);

            edtPertanyaan.setText(soal.getQuestion());

            List<String> opsi = soal.getOptions();
            String jawabanBenar = soal.getAnswer();
            String jawabanUserSekarang = (jawabanUser != null && i < jawabanUser.size()) ? jawabanUser.get(i) : null;

            if (opsi != null && opsi.size() >= 2) {
                opsi1.setText(opsi.get(0));
                opsi2.setText(opsi.get(1));

                boolean opsi3Kosong = opsi.size() > 2 && opsi.get(2).trim().isEmpty();
                boolean opsi4Kosong = opsi.size() > 3 && opsi.get(3).trim().isEmpty();

                if (opsi3Kosong && opsi4Kosong) {
                    opsi3.setVisibility(View.GONE);
                    opsi4.setVisibility(View.GONE);
                    check3.setVisibility(View.GONE);
                    check4.setVisibility(View.GONE);
                    teks_opsi3.setVisibility(View.GONE);
                    teks_opsi4.setVisibility(View.GONE);
                }

                opsi3.setText(opsi.get(2));
                opsi4.setText(opsi.get(3));

                setCheckImage(check1, opsi.get(0), jawabanBenar);
                setCheckImage(check2, opsi.get(1), jawabanBenar);
                if (!opsi3Kosong) setCheckImage(check3, opsi.get(2), jawabanBenar);
                if (!opsi4Kosong) setCheckImage(check4, opsi.get(3), jawabanBenar);

                if (jawabanUserSekarang != null) {
                    if (jawabanUserSekarang.equalsIgnoreCase(opsi.get(0))) {
                        opsi1.setBackgroundResource(
                                opsi.get(0).equalsIgnoreCase(jawabanBenar) ? R.drawable.rounded_klik : R.drawable.bg_susah
                        );
                    }
                    if (jawabanUserSekarang.equalsIgnoreCase(opsi.get(1))) {
                        opsi2.setBackgroundResource(
                                opsi.get(1).equalsIgnoreCase(jawabanBenar) ? R.drawable.rounded_klik : R.drawable.bg_susah
                        );
                    }
                    if (jawabanUserSekarang.equalsIgnoreCase(opsi.get(2))) {
                        opsi3.setBackgroundResource(
                                opsi.get(2).equalsIgnoreCase(jawabanBenar) ? R.drawable.rounded_klik : R.drawable.bg_susah
                        );
                    }
                    if (jawabanUserSekarang.equalsIgnoreCase(opsi.get(3))) {
                        opsi4.setBackgroundResource(
                                opsi.get(3).equalsIgnoreCase(jawabanBenar) ? R.drawable.rounded_klik : R.drawable.bg_susah
                        );
                    }
                }
            }

            containerPembahasan.addView(cardView);

            ImageView icLoading = cardView.findViewById(R.id.ic_loading);
            TextView gagal_memuat = cardView.findViewById(R.id.gagal_memuat);

            requestAIExplanation(soal.getQuestion(), pembahasan, icLoading, gagal_memuat);
        }
    }

    private void setCheckImage(ImageView imageView, String opsiText, String jawabanBenar) {
        if (opsiText.equalsIgnoreCase(jawabanBenar)) {
            imageView.setImageResource(R.drawable.accept);
            imageView.setImageTintList(null);
        } else {
            imageView.setImageResource(R.drawable.check);
        }
    }

    private void requestAIExplanation(String prompt, TextView pembahasanView, ImageView loadingGif, TextView gagalMemuat) {
        pembahasanView.setVisibility(View.GONE);
        gagalMemuat.setVisibility(View.GONE);
        loadingGif.setVisibility(View.VISIBLE);

        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(loadingGif);

        if (!isNetworkAvailable()) {
            tampilkanKesalahan(loadingGif, gagalMemuat, prompt, pembahasanView);
            return;
        }

        String escapedPrompt = escapeJson(prompt);
        String json = "{ \"contents\": [ { \"parts\": [ {\"text\": \"" + escapedPrompt + "\"} ] } ] }";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        apiService.getAIExplanation(body).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                loadingGif.setVisibility(View.GONE);
                gagalMemuat.setVisibility(View.GONE);
                pembahasanView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    String result = extractExplanationText(response.body());
                    pembahasanView.setText(!TextUtils.isEmpty(result) ? result : "Pembahasan tidak tersedia.");
                } else {
                    tampilkanErrorLog(response);
                    pembahasanView.setText("Gagal mendapatkan pembahasan.");
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                tampilkanKesalahan(loadingGif, gagalMemuat, prompt, pembahasanView);
            }
        });
    }

    private void tampilkanKesalahan(ImageView loadingGif, TextView gagalMemuat, String prompt, TextView pembahasanView) {
        loadingGif.setVisibility(View.VISIBLE);
        gagalMemuat.setVisibility(View.VISIBLE);
        pembahasanView.setVisibility(View.GONE);

        loadingGif.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                loadingGif.setOnClickListener(null); // hentikan loop klik
                requestAIExplanation(prompt, pembahasanView, loadingGif, gagalMemuat);
            }
        });
    }

    private void tampilkanErrorLog(Response<AIResponse> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
            Log.e("PembahasanActivity", "Error: " + response.code() + " " + response.message() + " | " + errorBody);
        } catch (Exception e) {
            Log.e("PembahasanActivity", "Exception while reading errorBody", e);
        }
    }

    private String extractExplanationText(AIResponse response) {
        if (response == null || response.candidates == null || response.candidates.isEmpty()) return null;
        AIResponse.Candidate candidate = response.candidates.get(0);
        if (candidate.content == null || candidate.content.parts == null) return null;

        StringBuilder result = new StringBuilder();
        for (AIResponse.Part part : candidate.content.parts) {
            if (part.text != null) {
                result.append(part.text);
            }
        }
        return result.toString();
    }

    private String escapeJson(String input) {
        return input == null ? "" : input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo active = cm.getActiveNetworkInfo();
        return active != null && active.isConnected();
    }
}
