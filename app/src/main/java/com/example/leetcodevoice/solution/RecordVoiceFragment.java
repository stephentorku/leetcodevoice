package com.example.leetcodevoice.solution;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.leetcodevoice.LeetCodeActivity;
import com.example.leetcodevoice.R;
import com.example.leetcodevoice.models.UserSolution;
import com.example.leetcodevoice.service.FireStoreService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class RecordVoiceFragment extends Fragment {

    private static final String TAG = "RecordVoiceFragment";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 201;

    private boolean permissionToRecordAudio = false;
    private boolean permissionToWriteStorage = false;

    private MediaRecorder mediaRecorder;
    private File audioFile;
    private FireStoreService fireStoreService;
    private String problemId;
    private StorageReference storageReference;

    private Button btnStartRecording, btnStopRecording, btnUploadAudio;

    public RecordVoiceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_voice, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            permissionToRecordAudio = true;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
        } else {
            permissionToWriteStorage = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAudio = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case REQUEST_WRITE_STORAGE_PERMISSION:
                permissionToWriteStorage = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (!permissionToRecordAudio || !permissionToWriteStorage) {
            Toast.makeText(getActivity(), "Permission denied! Recording cannot start.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            problemId = getArguments().getString("problemId");
        }

        btnStartRecording = view.findViewById(R.id.btnStartRecording);
        btnStopRecording = view.findViewById(R.id.btnStopRecording);
        btnUploadAudio = view.findViewById(R.id.btnUploadAudio);

        btnStartRecording.setOnClickListener(v -> startRecording());
        btnStopRecording.setOnClickListener(v -> stopRecording());
        btnUploadAudio.setOnClickListener(v -> uploadAudioToFirebase(problemId));
    }

    private void startRecording() {
        if (!permissionToRecordAudio) {
            Toast.makeText(getActivity(), "Permission denied! Cannot start recording.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String uniqueAudioFileName = "voice_note_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".3gp";

            audioFile = new File(getActivity().getExternalFilesDir(null), uniqueAudioFileName);

            if (!audioFile.exists()) {
                audioFile.createNewFile();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();

            btnStartRecording.setEnabled(false);
            btnStopRecording.setEnabled(true);

            Toast.makeText(getActivity(), "Recording started...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error starting recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            btnStopRecording.setEnabled(false);
            btnUploadAudio.setEnabled(true);

            Toast.makeText(getActivity(), "Recording stopped.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error stopping recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadAudioToFirebase(String problemId) {
        if (audioFile != null) {
            Uri audioUri = Uri.fromFile(audioFile);
            StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("solutions/audio/" + audioUri.getLastPathSegment());

            audioRef.putFile(audioUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            saveAudioUrlToFirestore(problemId, downloadUrl);
                        }).addOnFailureListener(e -> {
                            Log.e("Firebase", "Failed to get download URL", e);
                        });

                        Toast.makeText(getActivity(), "Audio uploaded successfully", Toast.LENGTH_SHORT).show();
                        Intent listPage = new Intent(getContext(), LeetCodeActivity.class);
                        startActivity(listPage);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Audio upload failed", e);
                        Toast.makeText(getActivity(), "Audio upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getActivity(), "No audio selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveAudioUrlToFirestore(String problemId, String downloadUrl) {
        fireStoreService = new FireStoreService();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        fireStoreService.getUserSolution(userId, problemId, new FireStoreService.FirestoreCallback<Optional<UserSolution>>() {
            @Override
            public void onCallback(Optional<UserSolution> data) {
                if (data.isPresent()) {
                    UserSolution existingSolution = data.get();
                    existingSolution.setImageUrl(downloadUrl);

                    FirebaseFirestore.getInstance()
                            .collection("solutions")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("problemId", problemId)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    DocumentReference solutionRef = querySnapshot.getDocuments().get(0).getReference();
                                    solutionRef.update("voiceNoteUrl", downloadUrl)
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Audio URL updated successfully"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to update audio URL", e));
                                }
                            });
                } else {
                    UserSolution newSolution = new UserSolution(userId, problemId, downloadUrl,null);
                    fireStoreService.addSolution(newSolution, new FireStoreService.FirestoreCallback<String>() {
                        @Override
                        public void onCallback(String solutionId) {
                            Log.d("Firestore", "New solution created with ID: " + solutionId);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("Firestore", "Error creating new solution", e);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firestore", "Error retrieving user solution", e);
            }
        });
    }

}
