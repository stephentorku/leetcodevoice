package com.example.leetcodevoice.auth;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.leetcodevoice.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailAndPasswordActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private RadioButton signUpRadio;
    private RadioButton signInRadio;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_and_password);

        mAuth = FirebaseAuth.getInstance();

        signUpRadio = findViewById(R.id.sign_up_radio);
        signInRadio = findViewById(R.id.sign_in_radio);
        radioGroup = findViewById(R.id.radio_group);

        if (savedInstanceState == null) {
            loadFragment(new SignUpFragment());
            signUpRadio.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.sign_up_radio) {
                loadFragment(new SignUpFragment());
            } else if (checkedId == R.id.sign_in_radio) {
                loadFragment(new SignInFragment());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null){
                reload();
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_sign, fragment)
                .commit();
    }

    public void reload() {
        // Handle user reload
    }

}