package com.example.projectfinalmobile.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KuisModel implements Parcelable {

    private int id;
    @SerializedName("image")
    private String id_image;
    private String title;
    private String category;
    private String type;
    private String difficulty;
    private List<PertanyaanModel> questions;

    private String userId;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }


    public KuisModel() {
    }


    public KuisModel(String id_image, String title, String category, String type, String difficulty, List<PertanyaanModel> questions) {
        this.id_image = id_image;
        this.title = title;
        this.category = category;
        this.type = type;
        this.difficulty = difficulty;
        this.questions = questions;
    }

    protected KuisModel(Parcel in) {
        id_image = in.readString();
        title = in.readString();
        category = in.readString();
        type = in.readString();
        difficulty = in.readString();
        questions = in.createTypedArrayList(PertanyaanModel.CREATOR);
    }

    public static final Creator<KuisModel> CREATOR = new Creator<KuisModel>() {
        @Override
        public KuisModel createFromParcel(Parcel in) {
            return new KuisModel(in);
        }

        @Override
        public KuisModel[] newArray(int size) {
            return new KuisModel[size];
        }
    };

    public String getId_Image() {
        return id_image;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<PertanyaanModel> getQuestions() {
        return questions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id_image);
        dest.writeString(title);
        dest.writeString(category);
        dest.writeString(type);
        dest.writeString(difficulty);
        dest.writeTypedList(questions);
    }

    public void setId_image(String id_image) {
        this.id_image = id_image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setType(String type) {
        this.type = type;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setQuestions(List<PertanyaanModel> questions) {
        this.questions = questions;
    }
}
