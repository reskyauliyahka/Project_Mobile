package com.example.projectfinalmobile.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.projectfinalmobile.Adapter.KuisAdapter;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;

import java.util.ArrayList;
import java.util.List;

public class DibuatFragment extends Fragment {

    private RecyclerView recyclerKuis;
    private KuisAdapter kuisAdapter;
    private List<KuisModel> kuisList;
    private KuisHelper kuisHelper;
    private ImageView icLoading;
    private TextView noData;

    private Spinner spinnerKategori, spinnerTipe, spinnerTingkat;
    private EditText cariEditText;

    public DibuatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dibuat, container, false);

        recyclerKuis = view.findViewById(R.id.recycler_dibuat);
        recyclerKuis.setLayoutManager(new LinearLayoutManager(getContext()));
        icLoading = view.findViewById(R.id.ic_loading);
        noData = view.findViewById(R.id.no_data);

        spinnerKategori = view.findViewById(R.id.kategori_kuis);
        spinnerTipe = view.findViewById(R.id.tipe_kuis);
        spinnerTingkat = view.findViewById(R.id.tingkat_kesulitan);
        cariEditText = view.findViewById(R.id.cari);

        Glide.with(this).asGif().load(R.drawable.loading).into(icLoading);

        kuisHelper = new KuisHelper(getContext());
        kuisHelper.open();

        kuisList = new ArrayList<>();
        kuisAdapter = new KuisAdapter(getContext(), kuisList);
        recyclerKuis.setAdapter(kuisAdapter);

        setupSpinners();
        setupSearchListener();

        loadKuisDibuatWithFilter();

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> kategoriAdapter = ArrayAdapter.createFromResource(getContext(), R.array.kategori_kuis, android.R.layout.simple_spinner_item);
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(kategoriAdapter);

        ArrayAdapter<CharSequence> tipeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.tipe_kuis, android.R.layout.simple_spinner_item);
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipe.setAdapter(tipeAdapter);

        ArrayAdapter<CharSequence> tingkatAdapter = ArrayAdapter.createFromResource(getContext(), R.array.tingkat_kesulitan, android.R.layout.simple_spinner_item);
        tingkatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTingkat.setAdapter(tingkatAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadKuisDibuatWithFilter();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerKategori.setOnItemSelectedListener(listener);
        spinnerTipe.setOnItemSelectedListener(listener);
        spinnerTingkat.setOnItemSelectedListener(listener);
    }

    private void setupSearchListener() {
        cariEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadKuisDibuatWithFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadKuisDibuatWithFilter() {
        icLoading.setVisibility(View.VISIBLE);
        noData.setVisibility(View.GONE);
        recyclerKuis.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(() -> {

            Context context = getContext();
            if (context == null) {
                icLoading.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
                recyclerKuis.setVisibility(View.GONE);
                return;
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userIdInt = sharedPreferences.getInt("user_id", -1);
            if (userIdInt == -1) {
                icLoading.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
                recyclerKuis.setVisibility(View.GONE);
                return;
            }
            String userId = String.valueOf(userIdInt);

            String selectedKategori = spinnerKategori.getSelectedItem().toString();
            String selectedTipe = spinnerTipe.getSelectedItem().toString();
            String selectedTingkat = spinnerTingkat.getSelectedItem().toString();
            String keyword = cariEditText.getText().toString().toLowerCase();

            String kategori = selectedKategori.equals("Kategori Kuis") ? null : selectedKategori;
            String tipe = selectedTipe.equals("Tipe Kuis") ? null : selectedTipe;
            String tingkat = selectedTingkat.equals("Tingkat Kesulitan") ? null : selectedTingkat;

            List<KuisModel> allUserKuis = kuisHelper.getKuisByUserId(userId);

            kuisList.clear();
            for (KuisModel kuis : allUserKuis) {
                boolean match = true;

                if (kategori != null && !kuis.getCategory().equalsIgnoreCase(kategori)) match = false;
                if (tipe != null && !kuis.getType().equalsIgnoreCase(tipe)) match = false;
                if (tingkat != null && !kuis.getDifficulty().equalsIgnoreCase(tingkat)) match = false;
                if (!keyword.isEmpty() && !kuis.getTitle().toLowerCase().contains(keyword)) match = false;

                if (match) {
                    kuisList.add(kuis);
                }
            }

            icLoading.setVisibility(View.GONE);

            if (kuisList.isEmpty()) {
                noData.setVisibility(View.VISIBLE);
                recyclerKuis.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.GONE);
                recyclerKuis.setVisibility(View.VISIBLE);
                kuisAdapter.notifyDataSetChanged();
            }

        }, 1000);
    }


    @Override
    public void onResume() {
        super.onResume();
        loadKuisDibuatWithFilter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (kuisHelper != null) kuisHelper.close();
    }
}
