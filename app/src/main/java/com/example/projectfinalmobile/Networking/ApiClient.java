package com.example.projectfinalmobile.Networking;

import retrofit2.Retrofit;

public class ApiClient {
    private static ApiService apiService;

    public static ApiService getApiService() {
        if (apiService == null) {
            Retrofit retrofit = RetrofitClient.getClient();
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
