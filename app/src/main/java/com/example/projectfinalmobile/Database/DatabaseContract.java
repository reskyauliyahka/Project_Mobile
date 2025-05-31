package com.example.projectfinalmobile.Database;

import android.provider.BaseColumns;

public class DatabaseContract {

    public static class Users implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String USERNAME = "username";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }

    public static class Kuis implements BaseColumns {
        public static final String TABLE_NAME = "kuis";
        public static final String USER_ID = "user_id";
        public static final String JUDUL = "judul";
        public static final String KATEGORI = "kategori";
        public static final String TIPE = "tipe";
        public static final String TINGKAT_KESULITAN = "tingkat_kesulitan";
        public static final String IMG_URL = "img_url";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }

    public static class Pertanyaan implements BaseColumns {
        public static final String TABLE_NAME = "pertanyaan";
        public static final String KUIS_ID = "kuis_id";
        public static final String PERTANYAAN = "pertanyaan";
        public static final String JAWABAN = "jawaban";

    }

    public static class OpsiJawaban implements BaseColumns {
        public static final String TABLE_NAME = "opsi_jawaban";
        public static final String PERTANYAAN_ID = "pertanyaan_id";
        public static final String TEKS = "teks";
    }

    public static class Favorit implements BaseColumns {
        public static final String TABLE_NAME = "favorit";
        public static final String USER_ID = "user_id";
        public static final String KUIS_ID = "kuis_id";
    }

    public static class AktivitasKuis implements BaseColumns {
        public static final String TABLE_NAME = "aktivitas_kuis";
        public static final String USER_ID = "user_id";
        public static final String KUIS_ID = "kuis_id";
        public static final String SKOR = "skor";
        public static final String TANGGAL = "tanggal_pengerjaan";
    }

}
