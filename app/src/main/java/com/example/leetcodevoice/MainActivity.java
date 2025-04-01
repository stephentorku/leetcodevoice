package com.example.leetcodevoice;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leetcodevoice.auth.EmailAndPasswordActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCurrentUser();
    }

    public void checkCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            Intent homePage = new Intent(this, LeetCodeActivity.class);
            startActivity(homePage);
        } else {

            Intent authPage = new Intent(this, EmailAndPasswordActivity.class);
            startActivity(authPage);
        }
    }

}