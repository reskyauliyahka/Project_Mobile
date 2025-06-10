package com.example.projectfinalmobile.Activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectfinalmobile.Adapter.KuisAdapter;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Networking.ApiService;
import com.example.projectfinalmobile.Networking.RetrofitClient;
import com.example.projectfinalmobile.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SemuaKuisActivity extends AppCompatActivity {

    private RecyclerView recyclerKuis;
    private KuisAdapter kuisAdapter;
    private ImageView btnKembali, icLoading;
    private TextView gagalMemuat, no_data;
    private Spinner spinnerKategori, spinnerTipe, spinnerTingkat;
    private EditText cariEditText;
    private KuisHelper kuisHelper;

    private List<KuisModel> allKuisList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semua_kuis);

        kuisHelper = new KuisHelper(this);

        recyclerKuis = findViewById(R.id.recyclekuis);
        recyclerKuis.setLayoutManager(new LinearLayoutManager(this));

        btnKembali = findViewById(R.id.btn_kembali);
        gagalMemuat = findViewById(R.id.gagal_memuat);
        icLoading = findViewById(R.id.ic_loading);
        no_data = findViewById(R.id.no_data);

        spinnerKategori = findViewById(R.id.kategori_kuis);
        spinnerTipe = findViewById(R.id.tipe_kuis);
        spinnerTingkat = findViewById(R.id.tingkat_kesulitan);
        cariEditText = findViewById(R.id.cari);

        Glide.with(this).asGif().load(R.drawable.loading).into(icLoading);

        btnKembali.setOnClickListener(v -> finish());
        icLoading.setOnClickListener(v -> loadAllKuisFromAPI());

        ArrayAdapter<CharSequence> kategoriAdapter = ArrayAdapter.createFromResource(this, R.array.kategori_kuis, android.R.layout.simple_spinner_item);
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(kategoriAdapter);

        ArrayAdapter<CharSequence> tipeAdapter = ArrayAdapter.createFromResource(this, R.array.tipe_kuis, android.R.layout.simple_spinner_item);
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipe.setAdapter(tipeAdapter);

        ArrayAdapter<CharSequence> tingkatAdapter = ArrayAdapter.createFromResource(this, R.array.tingkat_kesulitan, android.R.layout.simple_spinner_item);
        tingkatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTingkat.setAdapter(tingkatAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterKuis();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerKategori.setOnItemSelectedListener(filterListener);
        spinnerTipe.setOnItemSelectedListener(filterListener);
        spinnerTingkat.setOnItemSelectedListener(filterListener);

        cariEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterKuis();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAllKuisFromAPI();
    }

    private void loadAllKuisFromAPI() {
        icLoading.setVisibility(View.VISIBLE);
        gagalMemuat.setVisibility(View.GONE);
        no_data.setVisibility(View.GONE);
        recyclerKuis.setVisibility(View.GONE);

        if (!isNetworkAvailable()) {
            icLoading.setVisibility(View.VISIBLE);
            gagalMemuat.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.GONE);
            allKuisList.clear();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<KuisModel>> call = apiService.getAllKuis();

        call.enqueue(new Callback<List<KuisModel>>() {
            @Override
            public void onResponse(Call<List<KuisModel>> call, Response<List<KuisModel>> response) {
                icLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<KuisModel> apiKuisList = response.body();
                    List<KuisModel> localKuisList = loadLocalKuis();

                    Set<String> existingKeys = new HashSet<>();
                    List<KuisModel> combinedList = new ArrayList<>();

                    for (KuisModel kuis : apiKuisList) {
                        String key = generateKey(kuis);
                        existingKeys.add(key);
                        combinedList.add(kuis);
                    }

                    for (KuisModel kuis : localKuisList) {
                        String key = generateKey(kuis);
                        if (!existingKeys.contains(key)) {
                            combinedList.add(kuis);
                        }
                    }

                    allKuisList = combinedList;
                    filterKuis();
                } else {
                    gagalMemuat.setVisibility(View.VISIBLE);
                    no_data.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<KuisModel>> call, Throwable t) {
                icLoading.setVisibility(View.GONE);
                gagalMemuat.setVisibility(View.VISIBLE);
                no_data.setVisibility(View.GONE);
                recyclerKuis.setVisibility(View.GONE);
                allKuisList.clear(); // kosongkan agar tidak difilter
            }
        });
    }

    private void filterKuis() {
        if (!isNetworkAvailable() || allKuisList == null || allKuisList.isEmpty()) {
            recyclerKuis.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
            return;
        }

        icLoading.setVisibility(View.VISIBLE);
        recyclerKuis.setVisibility(View.GONE);
        no_data.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(() -> {
            String kategori = spinnerKategori.getSelectedItem().toString();
            String tipe = spinnerTipe.getSelectedItem().toString();
            String tingkat = spinnerTingkat.getSelectedItem().toString();
            String keyword = cariEditText.getText().toString().toLowerCase();

            List<KuisModel> filtered = new ArrayList<>();
            for (KuisModel kuis : allKuisList) {
                boolean matchKategori = kategori.equals("Kategori Kuis") || kuis.getCategory().equalsIgnoreCase(kategori);
                boolean matchTipe = tipe.equals("Tipe Kuis") || kuis.getType().equalsIgnoreCase(tipe);
                boolean matchTingkat = tingkat.equals("Tingkat Kesulitan") || kuis.getDifficulty().equalsIgnoreCase(tingkat);
                boolean matchCari = keyword.isEmpty() || kuis.getTitle().toLowerCase().contains(keyword);

                if (matchKategori && matchTipe && matchTingkat && matchCari) {
                    filtered.add(kuis);
                }
            }

            icLoading.setVisibility(View.GONE);

            if (filtered.isEmpty()) {
                no_data.setVisibility(View.VISIBLE);
                recyclerKuis.setVisibility(View.GONE);
            } else {
                no_data.setVisibility(View.GONE);
                recyclerKuis.setVisibility(View.VISIBLE);
                kuisAdapter = new KuisAdapter(this, filtered);
                recyclerKuis.setAdapter(kuisAdapter);
            }
        }, 1000);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllKuisFromAPI();
    }

    private List<KuisModel> loadLocalKuis() {
        return kuisHelper.getAllKuis2();
    }

    private String generateKey(KuisModel kuis) {
        return kuis.getTitle() + "|" +
                kuis.getCategory() + "|" +
                kuis.getType() + "|" +
                kuis.getDifficulty() + "|" +
                kuis.getId_Image() + "|" +
                kuis.getUserId();
    }
}
