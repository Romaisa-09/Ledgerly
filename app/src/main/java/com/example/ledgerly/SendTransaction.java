package com.example.ledgerly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SendTransaction extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST = 1001;

    private Button   btnBack, btnSend;
    private TextView etName, etAmount;
    private int vendorId, customerId;
    private String customerName, selectedCurrency;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction_ledgerly);

        ensureSmsPermission();

        btnBack  = findViewById(R.id.btnBackSendTransaction);
        btnSend  = findViewById(R.id.btnSendTransaction);
        etName   = findViewById(R.id.etNameSendTransaction);
        etAmount = findViewById(R.id.etAmountSendTransaction);

        vendorId         = getIntent().getIntExtra("user_id", -1);
        customerId       = getIntent().getIntExtra("customer_user_id", -1);
        customerName     = getIntent().getStringExtra("customer_name");
        selectedCurrency = getIntent().getStringExtra("selected_currency");

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String name   = etName.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError(getString(R.string.field_required));
            } else if (amount.isEmpty()) {
                etAmount.setError(getString(R.string.field_required));
            } else {
                String inRupees = toRupees(amount);
                if (!inRupees.equals("Invalid")) {
                    recordTransaction(name, inRupees);
                    adjustCustomerBalance(Integer.parseInt(inRupees));
                    String phone = new DatabaseHelperCustomer(this)
                            .open().getPhoneNumber(customerId);
                    if (phone != null) {
                        sendSms(phone, getString(
                                R.string.sms_template, name, amount, selectedCurrency
                        ));
                    }
                    startActivity(new Intent(this, SingleLedgerRecord.class)
                            .putExtra("customer_user_id", customerId)
                            .putExtra("customer_name", customerName)
                            .putExtra("user_id", vendorId)
                    );
                    finish();
                } else {
                    Toast.makeText(this,
                            R.string.invalid_amount, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void recordTransaction(String name, String amountStr) {
        int amt = Integer.parseInt(amountStr);
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        DatabaseHelperTransaction dbt = new DatabaseHelperTransaction(this);
        dbt.open();
        dbt.insertTransaction(vendorId, customerId, name, date, time, 1, 0, amt);
        dbt.close();

        Map<String, Object> tx = new HashMap<>();
        tx.put("_vendorid",   vendorId);
        tx.put("_customerid", customerId);
        tx.put("_name",       name);
        tx.put("_date",       date);
        tx.put("_time",       time);
        tx.put("_send",       1);
        tx.put("_receive",    0);
        tx.put("_amount",     amt);

        db.collection("Transaction_Table")
                .add(tx)
                .addOnSuccessListener(ref ->
                        Toast.makeText(this, R.string.tx_saved, Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.tx_save_failed, Toast.LENGTH_SHORT).show()
                );
    }

    private void adjustCustomerBalance(int added) {
        DatabaseHelperCustomer db = new DatabaseHelperCustomer(this);
        db.open();
        int current = db.getRemainingAmountForCustomer(customerId);
        db.updateCustomerRemainingAmount(customerId, current + added);
        db.close();
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        }
    }

    private String toRupees(String amtStr) {
        try {
            double val = Double.parseDouble(amtStr), r;
            switch (selectedCurrency) {
                case "Dollar": r = val * 278.05; break;
                case "Riyal":  r = val * 74.13;  break;
                case "Yen":    r = val * 1.82;   break;
                default:       r = val;          break;
            }
            return String.valueOf((int)Math.round(r));
        } catch (NumberFormatException e) {
            return "Invalid";
        }
    }
}
