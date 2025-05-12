package com.example.ledgerly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LedgerlyManager extends AppCompatActivity implements LedgerlyAdapter.ItemSelected {

    private static final String SHARED_PREFS = "com.example.ledgerly.shared_prefs";
    private static final String SELECTED_CURRENCY_KEY = "selected_currency";

    private int userId;
    private String selectedCurrency;
    private Button btnBack, btnAddCustomer;
    private RecyclerView rvLedger;
    private LedgerlyAdapter adapter;
    private ArrayList<Customer> customers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledgerly_manager);
        init();

        btnBack.setOnClickListener(v -> finish());
        btnAddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(LedgerlyManager.this, AddCustomer.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        });
    }

    private void init() {
        userId = getIntent().getIntExtra("user_id", -1);

        btnBack = findViewById(R.id.btnBackLedgerlyManager);
        btnAddCustomer = findViewById(R.id.btnAddCustomer);
        rvLedger = findViewById(R.id.rvLedger);
        rvLedger.setLayoutManager(new LinearLayoutManager(this));

        // Load customers
        DatabaseHelperCustomer db = new DatabaseHelperCustomer(this);
        db.open();
        customers = db.readAllCustomers(userId);
        db.close();

        // Retrieve currency
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        selectedCurrency = prefs.getString(SELECTED_CURRENCY_KEY, "Rupees");

        adapter = new LedgerlyAdapter(this, customers, prefs);
        rvLedger.setAdapter(adapter);
    }

    @Override
    public void onItemClicked(int index) {
        Customer c = customers.get(index);
        Toast.makeText(this, String.valueOf(c.getCid()), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LedgerlyManager.this, SingleLedgerRecord.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("selected_currency", selectedCurrency);
        intent.putExtra("customer_user_id", c.getCid());
        intent.putExtra("customer_name", c.getName());
        startActivity(intent);
        finish();
    }
}
