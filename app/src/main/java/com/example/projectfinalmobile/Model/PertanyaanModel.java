package com.example.projectfinalmobile.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class PertanyaanModel implements Parcelable {

    private int kuis_id;
    private String question;
    private List<String> options;
    private String answer;

    public PertanyaanModel() {

    }

    public PertanyaanModel(int kuis_id, String question, List<String> options, String answer) {
        this.kuis_id = kuis_id;
        this.question = question;
        this.options = options;
        this.answer = answer;
    }


    protected PertanyaanModel(Parcel in) {
        kuis_id = in.readInt();
        question = in.readString();
        options = in.createStringArrayList();
        answer = in.readString();
    }

    public static final Creator<PertanyaanModel> CREATOR = new Creator<PertanyaanModel>() {
        @Override
        public PertanyaanModel createFromParcel(Parcel in) {
            return new PertanyaanModel(in);
        }

        @Override
        public PertanyaanModel[] newArray(int size) {
            return new PertanyaanModel[size];
        }
    };

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getAnswer() {
        return answer;
    }
    public void setKuis_id(int kuis_id) {
        this.kuis_id = kuis_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(kuis_id);
        dest.writeString(question);
        dest.writeStringList(options);
        dest.writeString(answer);
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}

