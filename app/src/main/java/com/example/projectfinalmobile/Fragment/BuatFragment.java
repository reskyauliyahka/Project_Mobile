package com.example.projectfinalmobile.Fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;

public class BuatFragment extends Fragment implements InputDetailFragment.OnNextClickListener {

    private Button btnDetail, btnDaftar;

    public BuatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buat, container, false);

        btnDetail = view.findViewById(R.id.btn_detail);
        btnDaftar = view.findViewById(R.id.btn_daftar);

        Bundle args = getArguments();
        InputDetailFragment inputDetailFragment = new InputDetailFragment();

        if (args != null && args.containsKey("data_kuis")) {
            // Teruskan argumen ke InputDetailFragment
            inputDetailFragment.setArguments(args);
        }

        replaceFragment(inputDetailFragment);

        btnDetail.setOnClickListener(v -> {
            replaceFragment(new InputDetailFragment());
            btnDetail.setBackgroundResource(R.drawable.rounded_klik);
            btnDaftar.setBackgroundResource(R.drawable.rounded_unklik);
        });

        btnDaftar.setOnClickListener(v -> {
            replaceFragment(new InputDaftarFragment());
            btnDaftar.setBackgroundResource(R.drawable.rounded_klik);
            btnDetail.setBackgroundResource(R.drawable.rounded_unklik);
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container3, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void onNextClicked(KuisModel dataKuis, String judul, String kategori, String tipe, String tingkatKesulitan, Uri imgUri) {
        InputDaftarFragment daftarFragment = new InputDaftarFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("data_kuis", dataKuis); // kirim seluruh model
        bundle.putString("judul", judul);
        bundle.putString("kategori", kategori);
        bundle.putString("tipe", tipe);
        bundle.putString("tingkat_kesulitan", tingkatKesulitan);
        bundle.putString("img_url", imgUri != null ? imgUri.toString() : null);

        daftarFragment.setArguments(bundle);

        replaceFragment(daftarFragment);

        btnDaftar.setBackgroundResource(R.drawable.rounded_klik);
        btnDetail.setBackgroundResource(R.drawable.rounded_unklik);
    }

}
