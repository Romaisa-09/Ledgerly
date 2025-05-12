package com.example.ledgerly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kotlin.Triple;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin   = findViewById(R.id.btnLoginPage);
        btnBack    = findViewById(R.id.btnBackLogin);

        btnBack.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (email.isEmpty())    etEmail.setError("Field cannot be empty");
            if (password.isEmpty()) etPassword.setError("Field cannot be empty");
            else successfulLogin();
        });
    }

    private void successfulLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        DatabaseHelperVendor db = new DatabaseHelperVendor(this);
        db.open();

        Triple<Boolean, Integer, String> result = db.isValidUser(email, password);
        boolean isValid   = result.getFirst();
        int userId        = result.getSecond();
        String userName   = result.getThird();

        db.close();

        if (isValid) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, UserNameDisplay.class);
            intent.putExtra("user_name", userName);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}
