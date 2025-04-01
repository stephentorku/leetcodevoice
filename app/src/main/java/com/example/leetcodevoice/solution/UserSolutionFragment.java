package com.example.leetcodevoice.solution;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.leetcodevoice.R;
import com.example.leetcodevoice.models.LeetCodeProblem;
import com.example.leetcodevoice.models.UserSolution;
import com.example.leetcodevoice.service.FireStoreService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Optional;

public class UserSolutionFragment extends Fragment {

    private ImageView btnPlayPause, solutionImage;
    private SeekBar audioSeekBar;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private LeetCodeProblem problem;
    private FireStoreService fireStoreService;
    private UserSolution solution;

    private String audioUrl;
    private String imageUrl;

    public UserSolutionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_solution, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("UserSolutionFragment", "Fragment is displayed!");

        fireStoreService = new FireStoreService();

        if (getArguments() != null) {
            problem = getArguments().getParcelable("problem");
        }
        TextView heading = view.findViewById(R.id.heading);
        ViewGroup solutionContainer = view.findViewById(R.id.innerSolutionContainer);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("CurrentUser", currentUser.getEmail());
        Log.d("PROBLEMID", problem.getId());

        if (problem != null) {
            fireStoreService.getUserSolution(currentUser.getEmail(), problem.getId(), new FireStoreService.FirestoreCallback<Optional<UserSolution>>() {
                @Override
                public void onCallback(Optional<UserSolution> data) {
                    if (data.isPresent()) {
                        solution = data.get();
                        Log.d("UserSolutionFragment", "Solution found: ");

                        audioUrl = solution.getVoiceNoteUrl();
                        imageUrl = solution.getImageUrl();

                        Glide.with(UserSolutionFragment.this).load(imageUrl).into(solutionImage);

                        initializeMediaPlayer(audioUrl);
                    } else {
                        Log.d("UserSolutionFragment", "No solution found for this problem.");
                        solutionContainer.setVisibility(View.GONE);
                        heading.setText("No Solutions");
                        heading.setVisibility(View.VISIBLE);



                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("UserSolutionFragment", "Error fetching solution", e);
                }
            });
        } else {
            Log.e("UserSolutionFragment", "User not logged in or problem is null");
        }

        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        solutionImage = view.findViewById(R.id.solutionImage);
        audioSeekBar = view.findViewById(R.id.audioSeekBar);

        FloatingActionButton fabUploadSolution = view.findViewById(R.id.fab_add_solution);
        fabUploadSolution.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewSolutionActivity.class);
            intent.putExtra("problemId", problem.getId());
            startActivity(intent);
        });
    }

    private void initializeMediaPlayer(String url) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("MediaPlayer", "Error setting data source", e);
        }

        mediaPlayer.setOnPreparedListener(mp -> Log.d("MediaPlayer", "Audio is ready to play"));
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e("MediaPlayer", "Error playing audio: " + what);
            return true;
        });

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(R.drawable.ic_play_circle);
            } else {
                mediaPlayer.start();
                btnPlayPause.setImageResource(R.drawable.ic_pause_circle);
            }
            isPlaying = !isPlaying;
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            btnPlayPause.setImageResource(R.drawable.ic_play_circle);
            isPlaying = false;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
