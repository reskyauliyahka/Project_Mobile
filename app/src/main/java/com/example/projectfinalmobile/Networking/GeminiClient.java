package com.example.projectfinalmobile.Networking;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiClient {

    // URL dasar Gemini API, tanpa ?key=... (key dikirim header atau query parameter)
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // API KEY dari Google AI Studio, contoh:
    private static final String API_KEY = "AIzaSyCoEjGTJ6KFvuRO6WRmo4P_iPLFA2dv9KQ"; // ganti dengan kunci aslimu

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getGeminiService() {
        if (apiService == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            // Tambahkan API key sebagai query parameter
                            HttpUrl originalHttpUrl = original.url();
                            HttpUrl url = originalHttpUrl.newBuilder()
                                    .addQueryParameter("key", API_KEY)
                                    .build();

                            Request request = original.newBuilder()
                                    .url(url)
                                    .header("Content-Type", "application/json")
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        }
                    }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL + "/")  // Tambahkan "/" supaya Retrofit valid
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
