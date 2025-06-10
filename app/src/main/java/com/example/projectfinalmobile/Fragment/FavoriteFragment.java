package com.example.projectfinalmobile.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Helper.FavoritHelper;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerKuis;
    private KuisAdapter kuisAdapter;
    private List<KuisModel> kuisFavoritList;
    private FavoritHelper favoritHelper;
    private KuisHelper kuisHelper;
    private ImageView icLoading;
    private TextView noData;

    private Spinner spinnerKategori, spinnerTipe, spinnerTingkat;
    private EditText cariEditText;

    public FavoriteFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerKuis = view.findViewById(R.id.recycler_favorite);
        recyclerKuis.setLayoutManager(new LinearLayoutManager(getContext()));
        icLoading = view.findViewById(R.id.ic_loading);
        noData = view.findViewById(R.id.no_data);

        spinnerKategori = view.findViewById(R.id.kategori_kuis);
        spinnerTipe = view.findViewById(R.id.tipe_kuis);
        spinnerTingkat = view.findViewById(R.id.tingkat_kesulitan);
        cariEditText = view.findViewById(R.id.cari);

        Glide.with(this).asGif().load(R.drawable.loading).into(icLoading);

        favoritHelper = new FavoritHelper(getContext());
        favoritHelper.open();

        kuisHelper = new KuisHelper(getContext());
        kuisHelper.open();

        kuisFavoritList = new ArrayList<>();
        kuisAdapter = new KuisAdapter(getContext(), kuisFavoritList);
        recyclerKuis.setAdapter(kuisAdapter);

        kuisAdapter.setOnFavoritChangedListener(this::refreshData);

        setupSpinners();
        setupSearchListener();

        loadFavoritWithFilter();

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
                loadFavoritWithFilter();
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
                loadFavoritWithFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadFavoritWithFilter() {
        icLoading.setVisibility(View.VISIBLE);
        noData.setVisibility(View.GONE);
        recyclerKuis.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(() -> {
            Context context = getContext();
            if (context == null || !isAdded()) return;

            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("user_id", -1);
            if (userId == -1) return;

            kuisFavoritList.clear();

            String selectedKategori = spinnerKategori.getSelectedItem().toString();
            String selectedTipe = spinnerTipe.getSelectedItem().toString();
            String selectedTingkat = spinnerTingkat.getSelectedItem().toString();
            String keyword = cariEditText.getText().toString();

            String kategori = selectedKategori.equals("Kategori Kuis") ? null : selectedKategori;
            String tipe = selectedTipe.equals("Tipe Kuis") ? null : selectedTipe;
            String tingkat = selectedTingkat.equals("Tingkat Kesulitan") ? null : selectedTingkat;

            Cursor cursor = favoritHelper.getFavoritByUserId(userId);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int kuisId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Favorit.KUIS_ID));
                    KuisModel kuis = kuisHelper.getFullKuisByIdWithFilter(kuisId, kategori, tipe, tingkat, keyword);
                    if (kuis != null) {
                        kuisFavoritList.add(kuis);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            if (!isAdded()) return;

            icLoading.setVisibility(View.GONE);

            if (kuisFavoritList.isEmpty()) {
                noData.setVisibility(View.VISIBLE);
                recyclerKuis.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.GONE);
                recyclerKuis.setVisibility(View.VISIBLE);
                kuisAdapter.notifyDataSetChanged();
            }

        }, 1000);
    }


    private void refreshData() {
        loadFavoritWithFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favoritHelper != null) favoritHelper.close();
        if (kuisHelper != null) kuisHelper.close();
    }
}
