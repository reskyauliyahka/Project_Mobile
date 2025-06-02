package com.example.projectfinalmobile.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectfinalmobile.Adapter.KuisAdapter;
import com.example.projectfinalmobile.Helper.UserHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Networking.ApiService;
import com.example.projectfinalmobile.Networking.RetrofitClient;
import com.example.projectfinalmobile.R;

import java.util.List;
import com.example.projectfinalmobile.Activity.SemuaKuisActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerKuis;
    private KuisAdapter kuisAdapter;
    private TextView lihatSemua, gagalMemuat, username;
    private ImageView icLoading;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        username = view.findViewById(R.id.username);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId != -1) {
            UserHelper userHelper = new UserHelper(requireContext());
            userHelper.open();

            Cursor cursor = userHelper.getUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String fetchedUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));

                username.setText("Hai \n" +fetchedUsername);

                cursor.close();
            } else {
                Log.e("HomeFragment", "Gagal mengambil data user dari database");
            }

            userHelper.close();
        } else {
            Log.e("HomeFragment", "User ID tidak ditemukan di SharedPreferences");
        }

        recyclerKuis = view.findViewById(R.id.recyclekuis);
        recyclerKuis.setLayoutManager(new LinearLayoutManager(getContext()));

        lihatSemua = view.findViewById(R.id.lihatSemua);
        gagalMemuat = view.findViewById(R.id.gagal_memuat);
        icLoading = view.findViewById(R.id.ic_loading);

        Glide.with(this).asGif().load(R.drawable.loading).into(icLoading);

        icLoading.setOnClickListener(v -> {
            loadKuisFromAPI();
        });

        loadKuisFromAPI();

        lihatSemua.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SemuaKuisActivity.class);
            startActivity(intent);
        });


        return view;
    }

    private void loadKuisFromAPI() {
        icLoading.setVisibility(View.VISIBLE);
        gagalMemuat.setVisibility(View.GONE);
        recyclerKuis.setVisibility(View.GONE);

        if (!isNetworkAvailable()) {
            icLoading.setVisibility(View.VISIBLE);
            gagalMemuat.setVisibility(View.VISIBLE);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<KuisModel>> call = apiService.getAllKuis();

        call.enqueue(new Callback<List<KuisModel>>() {
            @Override
            public void onResponse(Call<List<KuisModel>> call, Response<List<KuisModel>> response) {
                icLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<KuisModel> allKuis = response.body();

                    List<KuisModel> limitedKuis = allKuis.size() >= 2
                            ? allKuis.subList(0, 2)
                            : allKuis;

                    kuisAdapter = new KuisAdapter(getContext(), limitedKuis);
                    recyclerKuis.setAdapter(kuisAdapter);

                    recyclerKuis.setVisibility(View.VISIBLE);
                    icLoading.setVisibility(View.GONE);
                    gagalMemuat.setVisibility(View.GONE);
                } else {
                    gagalMemuat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<KuisModel>> call, Throwable t) {
                icLoading.setVisibility(View.VISIBLE);
                gagalMemuat.setVisibility(View.VISIBLE);
                recyclerKuis.setVisibility(View.GONE);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadKuisFromAPI();
    }
}
