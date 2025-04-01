package com.example.leetcodevoice.problem;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.leetcodevoice.R;
import com.example.leetcodevoice.models.LeetCodeProblem;
import com.example.leetcodevoice.service.FireStoreService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProblemListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProblemListFragment extends Fragment {
    private RecyclerView recyclerView;
    private LeetCodeProblemAdapter adapter;
    private List<LeetCodeProblem> problemList = new ArrayList<>();
    private FireStoreService fireStoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_problem_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fireStoreService = new FireStoreService();
        adapter = new LeetCodeProblemAdapter(problemList, this::onProblemClicked);
        recyclerView.setAdapter(adapter);

        fetchProblems();

        FloatingActionButton addProblem = view.findViewById(R.id.fab_add_problem);
        addProblem.setOnClickListener(v -> {
            Intent addProblemActivity = new Intent(getActivity(), NewProblemActivity.class);
            startActivity(addProblemActivity);
        });

        return view;
    }

    private void fetchProblems() {
        fireStoreService.getAllProblems(problems -> {
            problemList.clear();
            problemList.addAll(problems);
            adapter.notifyDataSetChanged();
        });
    }

    private void onProblemClicked(LeetCodeProblem problem) {
        Toast.makeText(getContext(), "Clicked: " + problem.getTitle(), Toast.LENGTH_SHORT).show();

        Bundle bundle = new Bundle();
        bundle.putParcelable("problem", problem);

        ProblemDetailFragment detailFragment = new ProblemDetailFragment();
        detailFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_leet, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}
