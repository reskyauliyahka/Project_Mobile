package com.example.projectfinalmobile.Activity;

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

import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Model.PertanyaanModel;
import com.example.projectfinalmobile.Networking.AIResponse;
import com.example.projectfinalmobile.Networking.ApiService;
import com.example.projectfinalmobile.Networking.OpenAIApiClient;
import com.example.projectfinalmobile.R;
import com.google.gson.Gson;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembahasan);

        containerPembahasan = findViewById(R.id.container_pembahasan);
        inflater = LayoutInflater.from(this);

        apiService = OpenAIApiClient.getGeminiService();

        KuisModel kuis = getIntent().getParcelableExtra("data_kuis");

        if (kuis != null && kuis.getQuestions() != null) {
            tampilkanSemuaSoal(kuis.getQuestions());
        }
    }

    private void tampilkanSemuaSoal(List<PertanyaanModel> listSoal) {
        for (PertanyaanModel soal : listSoal) {
            View cardView = inflater.inflate(R.layout.card_pembahasan, containerPembahasan, false);

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

            // Disable all EditText inputs
            edtPertanyaan.setEnabled(false);
            opsi1.setEnabled(false);
            opsi2.setEnabled(false);
            opsi3.setEnabled(false);
            opsi4.setEnabled(false);

            edtPertanyaan.setText(soal.getQuestion());

            List<String> opsi = soal.getOptions();
            if (opsi != null && opsi.size() >= 4) {
                opsi1.setText(opsi.get(0));
                opsi2.setText(opsi.get(1));
                opsi3.setText(opsi.get(2));
                opsi4.setText(opsi.get(3));

                String jawabanBenar = soal.getAnswer();

                setCheckImage(check1, opsi.get(0), jawabanBenar);
                setCheckImage(check2, opsi.get(1), jawabanBenar);
                setCheckImage(check3, opsi.get(2), jawabanBenar);
                setCheckImage(check4, opsi.get(3), jawabanBenar);
            }

            containerPembahasan.addView(cardView);

            requestAIExplanation(soal.getQuestion(), pembahasan);
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

    private void requestAIExplanation(String prompt, TextView pembahasanTextView) {
        // Jangan lupa escape karakter khusus di prompt
        String escapedPrompt = escapeJson(prompt);

        String json = "{"
                + "\"contents\": ["
                + "  {"
                + "    \"parts\": ["
                + "      {\"text\": \"" + escapedPrompt + "\"}"
                + "    ]"
                + "  }"
                + "]"
                + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        apiService.getAIExplanation(body).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                if (response.isSuccessful()) {
                    AIResponse body = response.body();
                    if (body != null) {
                        Log.d("PembahasanActivity", "Response body: " + new Gson().toJson(body));
                        String resultText = extractExplanationText(body);
                        if (!TextUtils.isEmpty(resultText)) {
                            pembahasanTextView.setText(resultText);
                        } else {
                            pembahasanTextView.setText("Pembahasan tidak tersedia.");
                        }
                    } else {
                        pembahasanTextView.setText("Response body kosong.");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("PembahasanActivity", "Response error: " + response.code() + " " + response.message() + ", details: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pembahasanTextView.setText("Gagal mendapatkan pembahasan.");
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                pembahasanTextView.setText("Error: " + t.getMessage());
            }
        });
    }


    private String extractExplanationText(AIResponse response) {
        if (response == null || response.candidates == null || response.candidates.isEmpty()) {
            return null;
        }

        AIResponse.Candidate candidate = response.candidates.get(0);
        if (candidate.content == null || candidate.content.parts == null || candidate.content.parts.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (AIResponse.Part part : candidate.content.parts) {
            if (part.text != null) {
                sb.append(part.text);
            }
        }

        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
