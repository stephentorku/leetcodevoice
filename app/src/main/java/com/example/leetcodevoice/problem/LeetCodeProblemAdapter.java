package com.example.leetcodevoice.problem;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leetcodevoice.R;
import com.example.leetcodevoice.models.LeetCodeProblem;

import java.util.List;
public class LeetCodeProblemAdapter extends RecyclerView.Adapter<LeetCodeProblemAdapter.ViewHolder> {
    private final List<LeetCodeProblem> problems;
    private final OnProblemClickListener listener;

    public interface OnProblemClickListener {
        void onProblemClick(LeetCodeProblem problem);
    }

    public LeetCodeProblemAdapter(List<LeetCodeProblem> problems, OnProblemClickListener listener) {
        this.problems = problems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_problem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeetCodeProblem problem = problems.get(position);

        holder.tvTitle.setText(problem.getTitle());
        holder.tvDescription.setText(problem.getDescription());
        holder.tvDifficulty.setText(problem.getDifficulty());

        // Change difficulty background dynamically
        int color;
        switch (problem.getDifficulty().toLowerCase()) {
            case "easy":
                color = Color.parseColor("#4CAF50");
                break;
            case "medium":
                color = Color.parseColor("#FF9800");
                break;
            case "hard":
                color = Color.parseColor("#F44336");
                break;
            default:
                color = Color.parseColor("#9E9E9E");
        }
        holder.tvDifficulty.setBackgroundColor(color);

        holder.itemView.setOnClickListener(v -> listener.onProblemClick(problem));
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDifficulty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_problem_title);
            tvDescription = itemView.findViewById(R.id.tv_problem_description);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty_level);
        }
    }
}
