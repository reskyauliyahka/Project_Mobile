package com.example.projectfinalmobile.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;
import com.squareup.picasso.Picasso;

public class InputDetailFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView img;
    private EditText judul;
    private Spinner kategori, tipe, tingkat_kesulitan;
    private Button selanjutnya;
    private Uri imageUri;
    private KuisModel kuis = null;


    public interface OnNextClickListener {
        void onNextClicked(KuisModel kuis, String judul, String kategori, String tipe, String tingkatKesulitan, Uri imgUri);
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

        Bundle args = getArguments();
        if (args != null && args.containsKey("data_kuis")) {
            kuis = args.getParcelable("data_kuis");
            if (kuis != null) {
                judul.setText(kuis.getTitle());

                // Set spinner selection
                if (kuis.getCategory() != null) {
                    int kategoriPos = adapterKategori.getPosition(kuis.getCategory());
                    if (kategoriPos >= 0) kategori.setSelection(kategoriPos);
                }

                if (kuis.getType() != null) {
                    int tipePos = adapterTipe.getPosition(kuis.getType());
                    if (tipePos >= 0) tipe.setSelection(tipePos);
                }

                if (kuis.getDifficulty() != null) {
                    int tingkatPos = adapterTingkat.getPosition(kuis.getDifficulty());
                    if (tingkatPos >= 0) tingkat_kesulitan.setSelection(tingkatPos);
                }

                if (kuis.getId_Image() != null && !kuis.getId_Image().isEmpty()) {
                    Picasso.get()
                            .load(kuis.getId_Image())
                            .placeholder(R.drawable.logout)
                            .error(R.drawable.accept)
                            .fit()
                            .centerCrop()
                            .into(img);

                    imageUri = Uri.parse(kuis.getId_Image());
                }
            }
        }

        img.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        selanjutnya.setOnClickListener(v -> {
            String judulText = judul.getText().toString().trim();
            String kategoriText = kategori.getSelectedItem().toString();
            String tipeText = tipe.getSelectedItem().toString();
            String tingkatText = tingkat_kesulitan.getSelectedItem().toString();

            if (judulText.isEmpty() ||
                    kategoriText.equals("Kategori Kuis") ||
                    tipeText.equals("Tipe Kuis") ||
                    tingkatText.equals("Tingkat Kesulitan") ||
                    imageUri == null) {
                Toast.makeText(requireContext(), "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (kuis == null) {
                kuis = new KuisModel();
            }
            kuis.setTitle(judulText);
            kuis.setCategory(kategoriText);
            kuis.setType(tipeText);
            kuis.setDifficulty(tingkatText);
            if (imageUri != null) {
                kuis.setId_image(imageUri.toString());
            }

            listener.onNextClicked(kuis, judulText, kategoriText, tipeText, tingkatText, imageUri);
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            requireContext().getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            img.setImageURI(imageUri);
        }
    }
}
