package com.example.leetcodevoice.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.leetcodevoice.models.LeetCodeProblem;
import com.example.leetcodevoice.models.UserSolution;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FireStoreService {
    private final FirebaseFirestore db;
    private final Executor executor;
    private final Handler mainHandler;

    public FireStoreService() {
        db = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface FirestoreCallback<T> {
        void onCallback(T data);
        default void onError(Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllProblems(FirestoreCallback<List<LeetCodeProblem>> callback) {
        db.collection("problems")
                .get()
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        List<LeetCodeProblem> problems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            LeetCodeProblem problem = document.toObject(LeetCodeProblem.class);
                            problem.setId(document.getId());
                            problems.add(problem);
                        }
//                        Log.d("SER", problems.stream().toString());
                        mainHandler.post(() -> callback.onCallback(problems));
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
    }

    public void addProblem(LeetCodeProblem problem, FirestoreCallback<String> callback) {
        db.collection("problems")
                .add(problem)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        String docId = task.getResult().getId();  // Firestore generated ID
                        problem.setId(docId);  // Set the problem ID to the Firestore generated ID

                        // If you want to update the problem document with the ID, you can call update()
                        db.collection("problems").document(docId).set(problem)
                                .addOnCompleteListener(executor, updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        mainHandler.post(() -> callback.onCallback(docId));  // Return the document ID
                                    } else {
                                        mainHandler.post(() -> callback.onError(updateTask.getException()));
                                    }
                                });
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
    }

    public void addSolution(UserSolution solution, FirestoreCallback<String> callback) {
        db.collection("solutions")
                .add(solution)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        String docId = task.getResult().getId();
                        mainHandler.post(() -> callback.onCallback(docId));
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
    }

    public void getUserSolution(String userId, String problemId, FirestoreCallback<Optional<UserSolution>> callback) {
        db.collection("solutions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("problemId", problemId)
                .get()
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        List<UserSolution> solutions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserSolution solution = document.toObject(UserSolution.class);
                            solutions.add(solution);
                        }

                        Optional<UserSolution> result = solutions.isEmpty() ?
                                Optional.empty() : Optional.of(solutions.get(0));

                        mainHandler.post(() -> callback.onCallback(result));
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
    }


}