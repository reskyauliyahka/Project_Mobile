package com.example.projectfinalmobile.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.projectfinalmobile.R;

public class InputDetailFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView img;
    private EditText judul;
    private Spinner kategori, tipe, tingkat_kesulitan;
    private Button selanjutnya;
    private Uri imageUri;

    // Interface untuk callback ke BuatFragment
    public interface OnNextClickListener {
        void onNextClicked(String judul, String kategori, String tipe, String tingkatKesulitan, Uri imgUri);
    }

    private OnNextClickListener listener;

    public InputDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnNextClickListener) {
            listener = (OnNextClickListener) parentFragment;
        } else {
            throw new RuntimeException("Parent fragment must implement OnNextClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_detail, container, false);

        img = view.findViewById(R.id.img);
        judul = view.findViewById(R.id.judul_kuis);
        kategori = view.findViewById(R.id.kategori_kuis);
        tipe = view.findViewById(R.id.tipe_kuis);
        tingkat_kesulitan = view.findViewById(R.id.tingkat_kesulitan);
        selanjutnya = view.findViewById(R.id.selanjutnya);

        // Setup Spinner dengan adapter dari string-array
        ArrayAdapter<CharSequence> adapterKategori = ArrayAdapter.createFromResource(
                requireContext(), R.array.kategori_kuis, android.R.layout.simple_spinner_item);
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategori.setAdapter(adapterKategori);

        ArrayAdapter<CharSequence> adapterTipe = ArrayAdapter.createFromResource(
                requireContext(), R.array.tipe_kuis, android.R.layout.simple_spinner_item);
        adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipe.setAdapter(adapterTipe);

        ArrayAdapter<CharSequence> adapterTingkat = ArrayAdapter.createFromResource(
                requireContext(), R.array.tingkat_kesulitan, android.R.layout.simple_spinner_item);
        adapterTingkat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tingkat_kesulitan.setAdapter(adapterTingkat);

        // Pilih gambar dari galeri
        img.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        selanjutnya.setOnClickListener(v -> {
            String judulText = judul.getText().toString().trim();
            String kategoriText = kategori.getSelectedItem().toString();
            String tipeText = tipe.getSelectedItem().toString();
            String tingkatText = tingkat_kesulitan.getSelectedItem().toString();

            // Validasi input sederhana
            if (judulText.isEmpty() ||
                    kategoriText.equals("Kategori Kuis") ||
                    tipeText.equals("Tipe Kuis") ||
                    tingkatText.equals("Tingkat Kesulitan")) {
                Toast.makeText(requireContext(), "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kirim data ke parent fragment via callback
            listener.onNextClicked(judulText, kategoriText, tipeText, tingkatText, imageUri);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            img.setImageURI(imageUri);
        }
    }
}
