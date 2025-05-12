package com.example.ledgerly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btn_login_mainactivity);
        btnRegister = findViewById(R.id.btn_register_mainactivity);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(SplashScreen.this, LoginActivity.class))
        );

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(SplashScreen.this, RegistrationActivity.class))
        );
    }
}
