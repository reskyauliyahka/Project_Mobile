package com.example.projectfinalmobile.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectfinalmobile.Adapter.KuisAdapter;
import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Helper.AktivitasKuisHelper;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;

import java.util.ArrayList;
import java.util.List;

public class SelesaiFragment extends Fragment {

    private RecyclerView recyclerKuis;
    private KuisAdapter kuisAdapter;
    private List<KuisModel> kuisRiwayatList;
    private AktivitasKuisHelper aktivitasHelper;
    private KuisHelper kuisHelper;
    private ImageView icLoading;
    private TextView noData;

    private Spinner spinnerKategori, spinnerTipe, spinnerTingkat;
    private EditText cariEditText;

    private Handler handler;
    private Runnable loadRunnable;

    public SelesaiFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selesai, container, false);

        recyclerKuis = view.findViewById(R.id.recycler_history);
        recyclerKuis.setLayoutManager(new LinearLayoutManager(getContext()));
        icLoading = view.findViewById(R.id.ic_loading);
        noData = view.findViewById(R.id.no_data);
        spinnerKategori = view.findViewById(R.id.kategori_kuis);
        spinnerTipe = view.findViewById(R.id.tipe_kuis);
        spinnerTingkat = view.findViewById(R.id.tingkat_kesulitan);
        cariEditText = view.findViewById(R.id.cari);

        Glide.with(this).asGif().load(R.drawable.loading).into(icLoading);

        aktivitasHelper = new AktivitasKuisHelper(getContext());
        aktivitasHelper.open();

        if (getContext() != null) {
            kuisHelper = new KuisHelper(getContext());
            kuisHelper.open();
        }


        kuisRiwayatList = new ArrayList<>();
        kuisAdapter = new KuisAdapter(getContext(), kuisRiwayatList);
        recyclerKuis.setAdapter(kuisAdapter);

        setupSpinners();
        setupSearchListener();

        handler = new Handler(Looper.getMainLooper());

        loadHistoryWithFilter();

        return view;
    }

    private void setupSpinners() {
        Context context = getContext();
        if (context == null) return;

        ArrayAdapter<CharSequence> kategoriAdapter = ArrayAdapter.createFromResource(context, R.array.kategori_kuis, android.R.layout.simple_spinner_item);
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(kategoriAdapter);

        ArrayAdapter<CharSequence> tipeAdapter = ArrayAdapter.createFromResource(context, R.array.tipe_kuis, android.R.layout.simple_spinner_item);
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipe.setAdapter(tipeAdapter);

        ArrayAdapter<CharSequence> tingkatAdapter = ArrayAdapter.createFromResource(context, R.array.tingkat_kesulitan, android.R.layout.simple_spinner_item);
        tingkatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTingkat.setAdapter(tingkatAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadHistoryWithFilter();
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
                loadHistoryWithFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHistoryWithFilter() {
        icLoading.setVisibility(View.VISIBLE);
        noData.setVisibility(View.GONE);
        recyclerKuis.setVisibility(View.GONE);

        if (handler != null && loadRunnable != null) {
            handler.removeCallbacks(loadRunnable);
        }

        loadRunnable = () -> {
            if (!isAdded() || getContext() == null) return;

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("user_id", -1);
            if (userId == -1) return;

            kuisRiwayatList.clear();

            String selectedKategori = spinnerKategori.getSelectedItem().toString();
            String selectedTipe = spinnerTipe.getSelectedItem().toString();
            String selectedTingkat = spinnerTingkat.getSelectedItem().toString();
            String keyword = cariEditText.getText().toString();

            String kategori = selectedKategori.equals("Kategori Kuis") ? null : selectedKategori;
            String tipe = selectedTipe.equals("Tipe Kuis") ? null : selectedTipe;
            String tingkat = selectedTingkat.equals("Tingkat Kesulitan") ? null : selectedTingkat;

            Cursor cursor = aktivitasHelper.getAktivitasByUserId(userId);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int kuisId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.AktivitasKuis.KUIS_ID));
                    KuisModel kuis = kuisHelper.getFullKuisByIdWithFilter(kuisId, kategori, tipe, tingkat, keyword);
                    if (kuis != null) {
                        kuisRiwayatList.add(kuis);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            icLoading.setVisibility(View.GONE);

            if (kuisRiwayatList.isEmpty()) {
                noData.setVisibility(View.VISIBLE);
                recyclerKuis.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.GONE);
                recyclerKuis.setVisibility(View.VISIBLE);
                kuisAdapter.notifyDataSetChanged();
            }
        };

        handler.postDelayed(loadRunnable, 500);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryWithFilter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (handler != null && loadRunnable != null) {
            handler.removeCallbacks(loadRunnable);
        }

        if (aktivitasHelper != null) aktivitasHelper.close();
        if (kuisHelper != null) kuisHelper.close();
    }
}
