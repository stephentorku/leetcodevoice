package com.example.leetcodevoice.problem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.leetcodevoice.R;
import com.example.leetcodevoice.models.LeetCodeProblem;
import com.example.leetcodevoice.solution.UserSolutionFragment;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProblemDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProblemDetailFragment extends Fragment {
    private LeetCodeProblem problem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_problem_detail, container, false);

        if (getArguments() != null) {
            problem = getArguments().getParcelable("problem");
        }

        if (problem != null) {
            TextView titleTextView = view.findViewById(R.id.tv_problem_title);
            TextView descriptionTextView = view.findViewById(R.id.tv_problem_description);
            TextView difficultyTextView = view.findViewById(R.id.tv_difficulty_level);
            TextView categoryTextView = view.findViewById(R.id.tv_problem_category);
            TextView urlTextView = view.findViewById(R.id.tv_problem_url);

            String title = "Title: " +problem.getTitle();
            titleTextView.setText(title);

            String description = "Description: " +problem.getDescription();
            descriptionTextView.setText(description);

            String difficulty = "Difficulty: " +problem.getDifficulty();
            difficultyTextView.setText(difficulty);

            String category = "Category: " + problem.getCategory();
            categoryTextView.setText(category);

            String url = "External URL: " +problem.getUrl();
            urlTextView.setText(url);

            urlTextView.setOnClickListener(v -> getExternalWebsite(view));


            if (problem.getDifficulty().equalsIgnoreCase("easy")) {
                difficultyTextView.setBackgroundResource(R.color.chip_background);
            } else if (problem.getDifficulty().equalsIgnoreCase("medium")) {
                difficultyTextView.setBackgroundResource(R.color.chip_background);
            } else {
                difficultyTextView.setBackgroundResource(R.color.chip_background);
            }
        }

        loadSolutionFragment();

        return view;
    }

    private void loadSolutionFragment() {
        UserSolutionFragment solutionFragment = new UserSolutionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("problem", problem);
        solutionFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.solution_fragment_container, solutionFragment)
                .commit();
    }

    private void getExternalWebsite(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(problem.getUrl()));
        startActivity(intent);

    }
}
