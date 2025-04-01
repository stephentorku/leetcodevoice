package com.example.leetcodevoice.models;

import com.google.firebase.Timestamp;

public class UserSolution {
    private String userId;
    private String problemId;
    private String voiceNoteUrl;
    private String imageUrl;
    private String transcribedText;
    private Timestamp createdAt;

    public UserSolution() {}

    public UserSolution(String userId, String problemId, String voiceNoteUrl, String imageUrl) {
        this.userId = userId;
        this.problemId = problemId;
        this.voiceNoteUrl = voiceNoteUrl;
        this.imageUrl = imageUrl;
        this.createdAt = Timestamp.now();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProblemId() { return problemId; }
    public void setProblemId(String problemId) { this.problemId = problemId; }
    public String getVoiceNoteUrl() { return voiceNoteUrl; }
    public void setVoiceNoteUrl(String voiceNoteUrl) { this.voiceNoteUrl = voiceNoteUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getTranscribedText() { return transcribedText; }
    public void setTranscribedText(String transcribedText) { this.transcribedText = transcribedText; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
