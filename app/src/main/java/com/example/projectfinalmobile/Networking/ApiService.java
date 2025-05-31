package com.example.projectfinalmobile.Networking;



import com.example.projectfinalmobile.Model.KuisModel;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("quizzes")
    Call<List<KuisModel>> getAllKuis();

    @POST("kuis")
    Call<KuisModel> insertKuis(@Body KuisModel kuis);

    @POST("favorite")
    Call<KuisModel> insertFavorite(@Body KuisModel favorite);
}

