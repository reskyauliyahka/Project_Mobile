package com.example.projectfinalmobile.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfinalmobile.Activity.DetailKuisActivity;
import com.example.projectfinalmobile.Activity.TinjauJawabanActivity;
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

        holder.tvTitle.setText(kuis.getTitle());
        holder.tvCategory.setText(kuis.getCategory());
        holder.tvDifficulty.setText(kuis.getDifficulty());
        holder.type.setText(kuis.getType());

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

        Picasso.get()
                .load(kuis.getId_Image())
                .placeholder(R.drawable.loading)
                .error(R.drawable.accept)
                .fit()
                .centerCrop()
                .into(holder.ivImage);

        holder.card_kuis.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailKuisActivity.class);
            intent.putExtra("data_kuis", kuis);
            context.startActivity(intent);
        });

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (!kuisHelper.isKuisExist(kuis.getTitle())) {
            SQLiteDatabase db = kuisHelper.getWritableDatabase();
            kuisHelper.insertKuisLengkap(kuis, db);
            Log.d("FAVORIT_DEBUG", "Menambahkan kuis ke tabel kuis dengan kuis_id: " + kuis.getId() + " oleh user_id: " + userId);

        }


        int kuisId = kuisHelper.getIdByTitle(kuis.getTitle());
        String status = kuisHelper.getStatusById(kuisId);
        kuis.setStatus(status);
        boolean isOwnedByUser = false;

        List<KuisModel> userKuisList = kuisHelper.getKuisByUserId(String.valueOf(userId));
        for (KuisModel userKuis : userKuisList) {
            if (userKuis.getId() == kuisId) {
                isOwnedByUser = true;
                break;
            }
        }

        if (isOwnedByUser) {
            holder.btn_favorit.setImageResource(
                    kuis.getStatus().equalsIgnoreCase("tutup") ? R.drawable.lock : R.drawable.unlock
            );

            holder.btn_favorit.setOnClickListener(v -> {
                String pesan = kuis.getStatus().equalsIgnoreCase("tutup") ?
                        "Apakah Anda ingin membuka kuis ini?" :
                        "Apakah Anda ingin menutup kuis ini?";

                String statusBaru = kuis.getStatus().equalsIgnoreCase("tutup") ? "buka" : "tutup";

                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null, false);

                TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
                Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                tvMessage.setText(pesan);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(dialogView)
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(context, R.color.white))
                    );
                }
                btnConfirm.setOnClickListener(v1 -> {
                    boolean sukses = kuisHelper.updateStatusKuis(kuis.getId(), statusBaru);
                    if (sukses) {
                        kuis.setStatus(statusBaru);
                        Toast.makeText(context, "Status berhasil diperbarui ke " + statusBaru, Toast.LENGTH_SHORT).show();
                        holder.btn_favorit.setImageResource(
                                statusBaru.equals("tutup") ? R.drawable.lock : R.drawable.unlock
                        );
                    } else {
                        Toast.makeText(context, "Gagal memperbarui status", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v1 -> dialog.dismiss());

                dialog.show();
            });

        } else {

            boolean isFavorit = favoritHelper.isFavorit(userId, kuisId);
            holder.btn_favorit.setImageResource(isFavorit ? R.drawable.bookmark : R.drawable.bookmark_kosong);

            holder.btn_favorit.setOnClickListener(v -> {
                if (userId == -1) {
                    Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }

                String pesan = isFavorit ?
                        "Apakah Anda ingin menghapus dari favorit?" :
                        "Apakah Anda ingin menambahkan ke favorit?";

                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null, false);
                TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
                Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                tvMessage.setText(pesan);

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(dialogView)
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(context, R.color.white))
                    );
                }

                btnConfirm.setOnClickListener(v1 -> {
                    if (!isFavorit) {
                        favoritHelper.insertFavorit(userId, kuisId);
                        holder.btn_favorit.setImageResource(R.drawable.bookmark);
                        Toast.makeText(context, "Berhasil menambahkan ke favorit!", Toast.LENGTH_SHORT).show();
                    } else {
                        favoritHelper.deleteFavorit(userId, kuisId);
                        holder.btn_favorit.setImageResource(R.drawable.bookmark_kosong);
                        Toast.makeText(context, "Berhasil menghapus dari favorit", Toast.LENGTH_SHORT).show();

                        if (favoritChangedListener != null) {
                            favoritChangedListener.onFavoritChanged();
                        }
                    }

                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v1 -> dialog.dismiss());

                dialog.show();
            });


        }


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
