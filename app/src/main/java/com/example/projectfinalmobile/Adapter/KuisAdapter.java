package com.example.projectfinalmobile.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfinalmobile.Activity.DetailKuisActivity;
import com.example.projectfinalmobile.Helper.FavoritHelper;
import com.example.projectfinalmobile.Helper.KuisHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class KuisAdapter extends RecyclerView.Adapter<KuisAdapter.KuisViewHolder> {

    private final List<KuisModel> kuisList;
    private final Context context;

    private final KuisHelper kuisHelper;
    private final FavoritHelper favoritHelper;

    public interface OnFavoritChangedListener {
        void onFavoritChanged();
    }

    private OnFavoritChangedListener favoritChangedListener;

    public void setOnFavoritChangedListener(OnFavoritChangedListener listener) {
        this.favoritChangedListener = listener;
    }


    public KuisAdapter(Context context, List<KuisModel> kuisList) {
        this.context = context;
        this.kuisList = kuisList;

        kuisHelper = new KuisHelper(context);
        favoritHelper = new FavoritHelper(context);

        kuisHelper.open();
        favoritHelper.open();
    }

    @NonNull
    @Override
    public KuisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kuis, parent, false);
        return new KuisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KuisViewHolder holder, int position) {
        KuisModel kuis = kuisList.get(position);

        // Set data ke UI
        holder.tvTitle.setText(kuis.getTitle());
        holder.tvCategory.setText(kuis.getCategory());
        holder.tvDifficulty.setText(kuis.getDifficulty());
        holder.type.setText(kuis.getType());

        // Atur warna dan background difficulty
        switch (kuis.getDifficulty()) {
            case "Mudah":
                holder.tvDifficulty.setTextColor(ContextCompat.getColor(context, R.color.utama));
                holder.tvDifficulty.setBackgroundResource(R.drawable.rounded_klik);
                break;
            case "Sedang":
                holder.tvDifficulty.setTextColor(ContextCompat.getColor(context, R.color.teks_sedang));
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_sedang);
                break;
            case "Sulit":
                holder.tvDifficulty.setTextColor(ContextCompat.getColor(context, R.color.teks_susah));
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_susah);
                break;
        }

        // Load image pakai Picasso
        Picasso.get()
                .load(kuis.getId_Image())
                .placeholder(R.drawable.logout)
                .error(R.drawable.accept)
                .fit()
                .centerCrop()
                .into(holder.ivImage);

        // Intent ke detail activity
        holder.card_kuis.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailKuisActivity.class);
            intent.putExtra("data_kuis", kuis);
            context.startActivity(intent);
        });

        // Ambil userId dari SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        // Masukkan kuis ke DB jika belum ada
        if (!kuisHelper.isKuisExist(kuis.getTitle())) {
            SQLiteDatabase db = kuisHelper.getWritableDatabase();
            kuisHelper.insertKuisLengkap(kuis, db);
        }


        int kuisId = kuisHelper.getIdByTitle(kuis.getTitle());
        boolean isFavorit = favoritHelper.isFavorit(userId, kuisId);

        holder.btn_favorit.setImageResource(isFavorit ? R.drawable.bookmark : R.drawable.bookmark_kosong);

        holder.btn_favorit.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);

            if (!isFavorit) {
                builder.setTitle("Tambah ke Favorit");
                builder.setMessage("Apakah Anda ingin menambahkan ke favorite?");
                builder.setPositiveButton("Ya", (dialog, which) -> {
                    favoritHelper.insertFavorit(userId, kuisId);
                    holder.btn_favorit.setImageResource(R.drawable.bookmark); // ikon favorit aktif
                    Toast.makeText(context, "Berhasil menambahkan ke favorit!", Toast.LENGTH_SHORT).show();
                });
            } else {
                builder.setTitle("Hapus dari Favorit");
                builder.setMessage("Apakah Anda ingin menghapus dari favorite?");
                builder.setPositiveButton("Ya", (dialog, which) -> {
                    favoritHelper.deleteFavorit(userId, kuisId);
                    holder.btn_favorit.setImageResource(R.drawable.bookmark_kosong);
                    Toast.makeText(context, "Berhasil menghapus dari favorit", Toast.LENGTH_SHORT).show();

                    // Beritahu listener (fragment) agar reload data
                    if (favoritChangedListener != null) {
                        favoritChangedListener.onFavoritChanged();
                    }
                });

            }

            builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            // Atur background color dari @color/white setelah dialog ditampilkan
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(context, R.color.white))
            );
        });


    }

    @Override
    public int getItemCount() {
        return kuisList.size();
    }

    public void closeHelpers() {
        kuisHelper.close();
        favoritHelper.close();
    }

    public static class KuisViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btn_favorit;
        TextView tvTitle, tvCategory, tvDifficulty, type;
        CardView card_kuis;

        public KuisViewHolder(@NonNull View itemView) {
            super(itemView);
            card_kuis = itemView.findViewById(R.id.card_kuis);
            ivImage = itemView.findViewById(R.id.img_kuis);
            tvTitle = itemView.findViewById(R.id.judul_kuis);
            tvCategory = itemView.findViewById(R.id.kategori_kuis);
            tvDifficulty = itemView.findViewById(R.id.tingkat_kesulitan);
            type = itemView.findViewById(R.id.tipe_kuis);
            btn_favorit = itemView.findViewById(R.id.tambah_favorit);
        }
    }

}
