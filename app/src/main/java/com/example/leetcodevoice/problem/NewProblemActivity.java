package com.example.leetcodevoice.problem;


import com.example.leetcodevoice.LeetCodeActivity;
import com.example.leetcodevoice.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.leetcodevoice.models.LeetCodeProblem;
import com.example.leetcodevoice.service.FireStoreService;

public class NewProblemActivity extends AppCompatActivity {

    private EditText edtTitle, edtDescription, edtDifficulty, edtCategory, edtUrl;
    private Button btnSaveProblem;
    private FireStoreService fireStoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_problem);

        fireStoreService = new FireStoreService();

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtDifficulty = findViewById(R.id.edtDifficulty);
        edtCategory = findViewById(R.id.edtCategory);
        edtUrl = findViewById(R.id.edtUrl);
        btnSaveProblem = findViewById(R.id.btnSaveProblem);

        btnSaveProblem.setOnClickListener(v -> saveProblemToFirestore());
    }

    private void saveProblemToFirestore() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String difficulty = edtDifficulty.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();
        String url = edtUrl.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || difficulty.isEmpty() || category.isEmpty() || url.isEmpty()) {
            Toast.makeText(NewProblemActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LeetCodeProblem problem = new LeetCodeProblem(title, description, difficulty, category, url);

        fireStoreService.addProblem(problem, new FireStoreService.FirestoreCallback<String>() {
            @Override
            public void onCallback(String docId) {
                Toast.makeText(NewProblemActivity.this, "Problem saved successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(NewProblemActivity.this, "Error saving problem", Toast.LENGTH_SHORT).show();
            }
        });

        Intent listPage = new Intent(this, LeetCodeActivity.class);
        startActivity(listPage);
    }

}