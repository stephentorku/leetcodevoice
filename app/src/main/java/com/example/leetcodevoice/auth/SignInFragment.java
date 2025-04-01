package com.example.leetcodevoice.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Patterns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.leetcodevoice.LeetCodeActivity;
import com.example.leetcodevoice.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static final String TAG = "SIGN IN";

    private EditText emailEditText, passwordEditText;
    private Button signInButton;

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.editSignInEmailAddress);
        passwordEditText = view.findViewById(R.id.editSignInPassword);
        signInButton = view.findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInputs(email, password)) {
                signInUser(email, password);
            }
        });

        return view;
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void signInUser(String email, String password) {
        if (mAuth == null) {
            Toast.makeText(requireContext(), "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");

                        if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()) {
                            Toast.makeText(requireContext(),
                                    "Please verify your email address",
                                    Toast.LENGTH_SHORT).show();
                        }

                        Intent homePage = new Intent(requireContext(), LeetCodeActivity.class);
                        startActivity(homePage);
                        requireActivity().finish();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(requireContext(),
                                "Authentication failed: " + getFirebaseErrorMessage(task.getException()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "Unknown error";
        String error = exception.getMessage();
        if (error.contains("no user record")) {
            return "Account not found";
        } else if (error.contains("password is invalid")) {
            return "Incorrect password";
        } else if (error.contains("network error")) {
            return "No internet connection";
        }
        return error;
    }
}