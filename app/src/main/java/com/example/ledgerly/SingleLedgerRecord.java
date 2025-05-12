package com.example.ledgerly;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class SingleLedgerRecord extends AppCompatActivity
        implements LedgerlyTransactionAdapter.ItemSelected {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final String SHARED_PREFS = "com.example.ledgerly.shared_prefs";
    private static final String SELECTED_CURRENCY_KEY = "selected_currency";

    private Button btnBack, btnSend, btnReceive, btnPdf;
    private TextView tvCustomerName;
    private String customerName, selectedCurrency;
    private int customerId, vendorId;

    private RecyclerView rvTransactions;
    private LinearLayoutManager layoutManager;
    private LedgerlyTransactionAdapter adapter;
    private ArrayList<Transaction> transactions;
    private String startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_ledger_record);
        initViews();

        btnBack.setOnClickListener(v -> finish());

        tvCustomerName.setText(
                customerName != null ? customerName : getString(R.string.unknown_user)
        );

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        selectedCurrency = prefs.getString(SELECTED_CURRENCY_KEY, "Rupees");

        btnSend.setOnClickListener(v -> openSendActivity());
        btnReceive.setOnClickListener(v -> openReceiveActivity());
        btnPdf.setOnClickListener(v -> showDateRangePicker());
    }

    private void initViews() {
        customerName = getIntent().getStringExtra("customer_name");
        selectedCurrency = getIntent().getStringExtra("selected_currency");
        customerId = getIntent().getIntExtra("customer_user_id", -1);
        vendorId   = getIntent().getIntExtra("user_id", -1);

        btnBack = findViewById(R.id.btnBackSingleRecord);
        btnSend = findViewById(R.id.btnSend);
        btnReceive = findViewById(R.id.btnReceive);
        btnPdf = findViewById(R.id.btnPdfGenerator);
        tvCustomerName = findViewById(R.id.tvCustomerName);

        rvTransactions = findViewById(R.id.rvSingleRecord);
        layoutManager = new LinearLayoutManager(this);
        rvTransactions.setLayoutManager(layoutManager);

        DatabaseHelperTransaction db = new DatabaseHelperTransaction(this);
        db.open();
        transactions = db.readAllTransactions(customerId);
        db.close();

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        adapter = new LedgerlyTransactionAdapter(this, transactions, prefs, customerId);
        rvTransactions.setAdapter(adapter);
    }

    private void openSendActivity() {
        startActivity(new Intent(this, SendTransaction.class)
                .putExtra("user_id", vendorId)
                .putExtra("customer_user_id", customerId)
                .putExtra("customer_name", customerName)
                .putExtra("selected_currency", selectedCurrency)
        );
        finish();
    }

    private void openReceiveActivity() {
        startActivity(new Intent(this, ReceiveTransaction.class)
                .putExtra("user_id", vendorId)
                .putExtra("customer_user_id", customerId)
                .putExtra("customer_name", customerName)
                .putExtra("selected_currency", selectedCurrency)
        );
        finish();
    }

    @Override
    public void onItemClicked(int index) {
        Transaction tx = transactions.get(index);
        startActivity(new Intent(this, EditTransaction.class)
                .putExtra("user_id", vendorId)
                .putExtra("customer_user_id", customerId)
                .putExtra("customer_name", customerName)
                .putExtra("selected_currency", selectedCurrency)
                .putExtra("transaction_id", tx.getTid())
        );
        finish();
    }

    // Date pickers and PDF/SMS logic unchanged…
    private void showDateRangePicker() { /* … */ }
    private void showEndDatePicker() { /* … */ }
    private void generateAndSendTransactionDetails() { /* … */ }
    private void sendTransactionDetailsViaSMS(String message) { /* … */ }
}
