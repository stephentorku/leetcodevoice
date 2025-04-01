package com.example.leetcodevoice.solution;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.leetcodevoice.R;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;



public class NewSolutionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_solution);
        String problemId = getIntent().getStringExtra("problemId");
        Bundle bundle = new Bundle();
        bundle.putString("problemId", problemId);

        UploadImageFragment uploadImageFragment = new UploadImageFragment();
        uploadImageFragment.setArguments(bundle);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_upload_container,uploadImageFragment)
                    .commit();
        }

        FloatingActionButton fabSwitchFragment = findViewById(R.id.fabSwitchFragment);
        fabSwitchFragment.setOnClickListener(v -> {
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_upload_container) instanceof UploadImageFragment) {
                RecordVoiceFragment recordVoiceFragment = new RecordVoiceFragment();
                recordVoiceFragment.setArguments(bundle);
                switchFragment(recordVoiceFragment);
            } else {
                switchFragment(uploadImageFragment);
            }
        });

    }

    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_upload_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}