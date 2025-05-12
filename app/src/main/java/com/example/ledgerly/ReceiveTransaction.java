package com.example.ledgerly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReceiveTransaction extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST = 1001;

    private Button btnBack, btnReceive;
    private EditText etName, etAmount;
    private int vendorId, customerId;
    private String customerName, selectedCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_transaction_ledgerly);

        ensureSmsPermission();

        btnBack    = findViewById(R.id.btnBackReceiveTransaction);
        btnReceive = findViewById(R.id.btnReceiveTransaction);
        etName     = findViewById(R.id.etNameReceiveTransaction);
        etAmount   = findViewById(R.id.etAmountReceiveTransaction);

        vendorId         = getIntent().getIntExtra("user_id", -1);
        customerId       = getIntent().getIntExtra("customer_user_id", -1);
        customerName     = getIntent().getStringExtra("customer_name");
        selectedCurrency = getIntent().getStringExtra("selected_currency");

        btnBack.setOnClickListener(v -> finish());

        btnReceive.setOnClickListener(v -> {
            String name   = etName.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError(getString(R.string.field_required));
                return;
            }
            if (amount.isEmpty()) {
                etAmount.setError(getString(R.string.field_required));
                return;
            }

            String inRupees = toRupees(amount);
            if ("Invalid".equals(inRupees)) {
                Toast.makeText(this, R.string.invalid_amount, Toast.LENGTH_SHORT).show();
                return;
            }

            recordTransaction(name, Integer.parseInt(inRupees));
            adjustCustomerBalance(- Integer.parseInt(inRupees));

            DatabaseHelperCustomer dbc = new DatabaseHelperCustomer(this);
            dbc.open();
            String phone = dbc.getPhoneNumber(customerId);
            dbc.close();
            if (phone != null) {
                String sms = getString(
                        R.string.sms_template_receive,
                        name, amount, selectedCurrency
                );
                sendSms(phone, sms);
            }

            startActivity(new Intent(this, SingleLedgerRecord.class)
                    .putExtra("user_id", vendorId)
                    .putExtra("customer_user_id", customerId)
                    .putExtra("customer_name", customerName)
            );
            finish();
        });
    }

    private void recordTransaction(String name, int amt) {
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        DatabaseHelperTransaction dbt = new DatabaseHelperTransaction(this);
        dbt.open();
        dbt.insertTransaction(vendorId, customerId, name, date, time, 0, 1, amt);
        dbt.close();

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("Transaction_Table")
                .push();

        Map<String,Object> tx = new HashMap<>();
        tx.put("_vendorid",   vendorId);
        tx.put("_customerid", customerId);
        tx.put("_name",       name);
        tx.put("_date",       date);
        tx.put("_time",       time);
        tx.put("_send",       0);
        tx.put("_receive",    1);
        tx.put("_amount",     amt);

        ref.setValue(tx)
                .addOnSuccessListener(ignored ->
                        Toast.makeText(this, R.string.tx_saved, Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.tx_save_failed, Toast.LENGTH_SHORT).show()
                );
    }

    private void adjustCustomerBalance(int delta) {
        DatabaseHelperCustomer dbc = new DatabaseHelperCustomer(this);
        dbc.open();
        int current = dbc.getRemainingAmountForCustomer(customerId);
        dbc.updateCustomerRemainingAmount(customerId, current + delta);
        dbc.close();
    }

    private void sendSms(String phone, String msg) {
        try {
            SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
            Toast.makeText(this, R.string.sms_sent, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.sms_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void ensureSmsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST
            );
        }
    }

    private String toRupees(String s) {
        try {
            double v = Double.parseDouble(s), r;
            switch (selectedCurrency) {
                case "Dollar": r = v * 278.05; break;
                case "Riyal":  r = v * 74.13;  break;
                case "Yen":    r = v * 1.82;   break;
                default:       r = v;          break;
            }
            return String.valueOf((int)Math.round(r));
        } catch (NumberFormatException e) {
            return "Invalid";
        }
    }
}
