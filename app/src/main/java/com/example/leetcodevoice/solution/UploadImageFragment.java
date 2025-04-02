package com.example.leetcodevoice.solution;

import com.example.leetcodevoice.LeetCodeActivity;
import com.example.leetcodevoice.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.leetcodevoice.models.UserSolution;
import com.example.leetcodevoice.service.FireStoreService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Optional;

public class UploadImageFragment extends Fragment {

    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int PERMISSION_REQUEST_CODE = 102;

    private Button btnTakePhoto, btnUploadImage;
    private ImageView imageView;
    private String imageFilePath;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    String problemId;
    private FireStoreService fireStoreService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_image, container, false);
        if (getArguments() != null) {
            problemId = getArguments().getString("problemId");
        }

        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        imageView = view.findViewById(R.id.imageView);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        checkPermissions();

        btnTakePhoto.setOnClickListener(v -> takePicture());


        btnUploadImage.setOnClickListener(v -> uploadImageToFirebase(problemId));

        return view;
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            String uniqueFileName = "solution_image_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";

            File photoFile = new File(requireActivity().getExternalCacheDir(), uniqueFileName);
            imageFilePath = photoFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == getActivity().RESULT_OK) {
            File imgFile = new File(imageFilePath);
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile));
                btnUploadImage.setEnabled(true);
            }
        }
    }

    private void saveImageUrlToFirestore(String problemId, String downloadUrl) {
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
                                    solutionRef.update("imageUrl", downloadUrl)
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Image URL updated successfully"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to update image URL", e));
                                    Intent listPage = new Intent(getContext(), LeetCodeActivity.class);
                                    startActivity(listPage);
                                }
                            });
                } else {
                    UserSolution newSolution = new UserSolution(userId, problemId, null,downloadUrl);
                    fireStoreService.addSolution(newSolution, new FireStoreService.FirestoreCallback<String>() {
                        @Override
                        public void onCallback(String solutionId) {
                            Log.d("Firestore", "New solution created with ID: " + solutionId);
                            Intent listPage = new Intent(getContext(), LeetCodeActivity.class);
                            startActivity(listPage);
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



    private void uploadImageToFirebase(String problemId) {
        if (imageFilePath != null) {
            File imageFile = new File(imageFilePath);
            Uri imageUri = Uri.fromFile(imageFile);
            StorageReference imageRef = storageReference.child("solutions/images/" + imageUri.getLastPathSegment());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            Log.d("Firebase", "Image URL: " + downloadUrl);

                            saveImageUrlToFirestore(problemId, downloadUrl);

                            Toast.makeText(getActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.e("Firebase", "Failed to get download URL", e);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Error uploading image", e);
                    });
        } else {
            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getActivity(), "Permissions are required to capture image.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}