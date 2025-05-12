package com.example.ledgerly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddCustomer extends AppCompatActivity {

    private Button btnBack, btnAdd;
    private EditText etName, etPhone;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        userId = getIntent().getIntExtra("user_id", -1);

        btnBack = findViewById(R.id.btnBackAddCustomer);
        btnAdd  = findViewById(R.id.btnAddAddCustomer);
        etName  = findViewById(R.id.etNameAddCustomer);
        etPhone = findViewById(R.id.etPhoneAddCustomer);

        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            String name  = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            if (name.isEmpty())  etName.setError("Field cannot be empty");
            if (phone.isEmpty()) etPhone.setError("Field cannot be empty");
            if (!name.isEmpty() && !phone.isEmpty()) {
                insertCustomer(name, phone);
                startActivity(new Intent(AddCustomer.this, LedgerlyManager.class)
                        .putExtra("user_id", userId));
                finish();
            }
        });
    }

    private void insertCustomer(String name, String phone) {
        String formattedDate = new java.text.SimpleDateFormat("dd-MM-yyyy")
                .format(new java.util.Date());
        String formattedTime = new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new java.util.Date());

        DatabaseHelperCustomer db = new DatabaseHelperCustomer(this);
        db.open();
        db.insertCustomer(userId, name, formattedDate, formattedTime, phone);
        db.close();

        // Firestore part unchanged…
    }
}
