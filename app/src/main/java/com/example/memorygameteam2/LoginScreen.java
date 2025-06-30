package com.example.memorygameteam2;


import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class LoginScreen extends AppCompatActivity {
    private static final String TAG = "LoginScreen";
    private static final String USER_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String LOGIN_URL = "http://152.42.175.43/api/User/login?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLoginButton();
    }

    private void initLoginButton() {
        findViewById(R.id.login_btn).setOnClickListener(v -> {
            EditText userEditText = findViewById(R.id.User);
            EditText passwordEditText = findViewById(R.id.password);

            LoginHelper.performLogin(userEditText, passwordEditText, LOGIN_URL, new LoginHelper.LoginCallback() {
                @Override
                public void onLoginSuccess(String response) {
                    // Handle successful login
                    runOnUiThread(() -> {
                        Toast.makeText(LoginScreen.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        // Proceed to next activity or perform other actions

                        startActivity(new Intent(LoginScreen.this, MainActivity.class));
                    });
                }

                @Override
                public void onLoginFailure(String error) {
                    // Handle login failure
                    runOnUiThread(() -> {
                        Toast.makeText(LoginScreen.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}