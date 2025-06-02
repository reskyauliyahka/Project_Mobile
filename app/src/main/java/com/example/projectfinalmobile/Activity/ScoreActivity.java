package com.example.projectfinalmobile.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfinalmobile.R;
import android.widget.TextView;

public class ScoreActivity extends AppCompatActivity {
    private TextView txtScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        txtScore = findViewById(R.id.score);

        int score = getIntent().getIntExtra("score", 0);
        txtScore.setText(String.valueOf(score));
    }
}
