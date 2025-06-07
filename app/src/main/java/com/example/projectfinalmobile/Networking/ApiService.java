package com.example.projectfinalmobile.Networking;



import com.example.projectfinalmobile.Model.KuisModel;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {

    @GET("quizzes")
    Call<List<KuisModel>> getAllKuis();

    @POST(".")
    @Headers("Content-Type: application/json")
    Call<AIResponse> getAIExplanation(@Body RequestBody body);
}

