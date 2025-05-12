package com.example.ledgerly;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etUsername;
    private Button btnBack;
    private Button btnRegister;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_ledgerly);

        etUsername = findViewById(R.id.etUserNameRegistration);
        etEmail    = findViewById(R.id.etEmailAddressRegistration);
        etPassword = findViewById(R.id.etPasswordRegistration);
        btnBack    = findViewById(R.id.btnBackRegistration);
        btnRegister= findViewById(R.id.btnRegistrationPage);

        btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            if (email.isEmpty())    etEmail.setError("Field cannot be empty");
            if (password.isEmpty()) etPassword.setError("Field cannot be empty");
            if (username.isEmpty()) etUsername.setError("Field cannot be empty");
            if (!email.isEmpty() && !password.isEmpty() && !username.isEmpty()) {
                addVendor(username, email, password);
                finish();
            }
        });
    }

    private void addVendor(String username, String email, String password) {
        DatabaseHelperVendor dbHelper = new DatabaseHelperVendor(this);
        dbHelper.open();
        dbHelper.insert(username, email, password);
        dbHelper.close();

        Map<String, Object> user = new HashMap<>();
        user.put("_username", username);
        user.put("_email", email);
        user.put("_password", password);

        db.collection("Vendor_Table")
                .add(user)
                .addOnSuccessListener(docRef ->
                        Log.d("firebase1", "Added with ID: " + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.w("firebase1", "Error adding document", e));
    }
}
