package com.example.leetcodevoice.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class LeetCodeProblem implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String difficulty;
    private String category;
    private String url;

    public LeetCodeProblem() {}

    public LeetCodeProblem(String title, String description, String difficulty, String category, String url) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.category = category;
        this.url = url;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }

    protected LeetCodeProblem(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        difficulty = in.readString();
        category = in.readString();
        url = in.readString();
    }

    public static final Creator<LeetCodeProblem> CREATOR = new Creator<LeetCodeProblem>() {
        @Override
        public LeetCodeProblem createFromParcel(Parcel in) {
            return new LeetCodeProblem(in);
        }

        @Override
        public LeetCodeProblem[] newArray(int size) {
            return new LeetCodeProblem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(difficulty);
        dest.writeString(category);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "LeetCodeProblem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", category='" + category + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
