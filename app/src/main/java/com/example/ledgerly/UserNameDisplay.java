package com.example.ledgerly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserNameDisplay extends AppCompatActivity {

    private static final String SHARED_PREFS = "com.example.ledgerly.shared_prefs";
    private static final String SELECTED_CURRENCY_KEY = "selected_currency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_display_ledgerly);

        String username = getIntent().getStringExtra("user_name");
        int userId = getIntent().getIntExtra("user_id", -1);

        TextView tvUserNameShow = findViewById(R.id.tvUserNameShow);
        Spinner currencySpinner = findViewById(R.id.spinnerCurrency);
        Button btnMoveToLedgerly = findViewById(R.id.btnMoveToLedgerly);
        Button btnBack = findViewById(R.id.btnBackUserNameDisplay);

        tvUserNameShow.setText(username != null ? username : getString(R.string.unknown_user));

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        btnBack.setOnClickListener(v -> finish());

        btnMoveToLedgerly.setOnClickListener(v -> {
            String selectedCurrency = currencySpinner.getSelectedItem().toString();
            prefs.edit()
                    .putString(SELECTED_CURRENCY_KEY, selectedCurrency)
                    .apply();

            startActivity(new Intent(this, LedgerlyManager.class)
                    .putExtra("user_id", userId)
                    .putExtra("selected_currency", selectedCurrency)
            );
        });
    }
}
