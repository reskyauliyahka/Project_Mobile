package com.example.projectfinalmobile.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScoreActivity extends AppCompatActivity {
    private TextView txtScore;
    private Button btn_pembahasan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        txtScore = findViewById(R.id.score);

//        ArrayList<String> jawabanUser = getIntent().getStringArrayListExtra("jawaban_user");
        KuisModel kuis = getIntent().getParcelableExtra("data_kuis");


        int score = getIntent().getIntExtra("score", 0);
        txtScore.setText(String.valueOf(score));

        btn_pembahasan = findViewById(R.id.btn_pembahasan);
        btn_pembahasan.setOnClickListener(v -> {
            Intent intent = new Intent(this, PembahasanActivity.class);
            intent.putExtra("data_kuis", kuis);
            intent.putExtra("score", score);
//            intent.putStringArrayListExtra("jawaban_user", new ArrayList<>(jawabanUser));
            startActivity(intent);
        });

        ImageView btn_kembali = findViewById(R.id.btn_kembali);
        btn_kembali.setOnClickListener(v -> finish());
    }
}
