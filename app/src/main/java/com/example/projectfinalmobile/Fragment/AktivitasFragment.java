package com.example.projectfinalmobile.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.projectfinalmobile.R;

public class AktivitasFragment extends Fragment {

    private Button btnSelesai, btnDibuat;

    public AktivitasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_aktivitas, container, false);

        btnSelesai = view.findViewById(R.id.btn_selesai);
        btnDibuat = view.findViewById(R.id.btn_dibuat);

        replaceFragment(new SelesaiFragment());

        btnSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new SelesaiFragment());
                btnSelesai.setBackgroundResource(R.drawable.rounded_klik);
                btnDibuat.setBackgroundResource(R.drawable.rounded_unklik);
            }
        });

        btnDibuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new DibuatFragment());
                btnDibuat.setBackgroundResource(R.drawable.rounded_klik);
                btnSelesai.setBackgroundResource(R.drawable.rounded_unklik);
            }
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container2, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
