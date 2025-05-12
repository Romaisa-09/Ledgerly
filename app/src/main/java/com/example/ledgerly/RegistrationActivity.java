package com.example.ledgerly;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etUsername;
    private Button btnBack;
    private Button btnRegister;

    // Reference to your Realtime Database
    private final DatabaseReference dbRef =
            FirebaseDatabase.getInstance()
                    .getReference("Vendor_Table");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_ledgerly);

        etUsername    = findViewById(R.id.etUserNameRegistration);
        etEmail       = findViewById(R.id.etEmailAddressRegistration);
        etPassword    = findViewById(R.id.etPasswordRegistration);
        btnBack       = findViewById(R.id.btnBackRegistration);
        btnRegister   = findViewById(R.id.btnRegistrationPage);

        btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            boolean valid = true;
            if (email.isEmpty()) {
                etEmail.setError("Field cannot be empty");
                valid = false;
            }
            if (password.isEmpty()) {
                etPassword.setError("Field cannot be empty");
                valid = false;
            }
            if (username.isEmpty()) {
                etUsername.setError("Field cannot be empty");
                valid = false;
            }

            if (valid) {
                addVendor(username, email, password);
                finish();
            }
        });
    }

    private void addVendor(String username, String email, String password) {
        // 1. Insert locally
        DatabaseHelperVendor dbHelper = new DatabaseHelperVendor(this);
        dbHelper.open();
        dbHelper.insert(username, email, password);
        dbHelper.close();

        // 2. Prepare data for Realtime DB
        Map<String,Object> user = new HashMap<>();
        user.put("_username", username);
        user.put("_email",    email);
        user.put("_password", password);

        // 3. Push under Vendor_Table with auto-generated key
        String newKey = dbRef.push().getKey();
        if (newKey == null) {
            Log.w("RealtimeDB", "Could not generate key for new vendor");
            return;
        }

        dbRef.child(newKey)
                .setValue(user)
                .addOnSuccessListener(aVoid ->
                        Log.d("RealtimeDB", "Vendor added under key: " + newKey)
                )
                .addOnFailureListener(e ->
                        Log.w("RealtimeDB", "Error writing new vendor", e)
                );
    }
}
