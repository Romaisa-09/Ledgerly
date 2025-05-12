package com.example.ledgerly;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EditTransaction extends AppCompatActivity {

    private Button btnBack, btnDelete, btnUpdate;
    private EditText etName, etAmount;
    private int vendorId, customerId, transactionId;
    private String customerName, selectedCurrency;
    private String originalName;
    private int originalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction_ledgerly);

        btnBack   = findViewById(R.id.btnBackEditTransaction);
        btnDelete = findViewById(R.id.btnDeleteEditTransaction);
        btnUpdate = findViewById(R.id.btnUpdateEditTransaction);
        etName    = findViewById(R.id.etNameEditTransaction);
        etAmount  = findViewById(R.id.etAmountEditTransaction);

        customerName     = getIntent().getStringExtra("customer_name");
        selectedCurrency = getIntent().getStringExtra("selected_currency");
        customerId       = getIntent().getIntExtra("customer_user_id", -1);
        vendorId         = getIntent().getIntExtra("user_id", -1);
        transactionId    = getIntent().getIntExtra("transaction_id", -1);

        // Load original transaction
        DatabaseHelperTransaction dbt = new DatabaseHelperTransaction(this);
        dbt.open();
        originalName   = dbt.getName(transactionId);
        originalAmount = dbt.getAmount(transactionId);
        dbt.close();

        etName.setText(originalName);
        etAmount.setText(getConvertedAmount(originalAmount));

        btnBack.setOnClickListener(v -> finish());

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.delete_prompt)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    deleteTransactionAndAdjust();
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
        );

        btnUpdate.setOnClickListener(v -> {
            String nameNew = etName.getText().toString().trim();
            String amtNew  = etAmount.getText().toString().trim();
            int changedAmt = Integer.parseInt(getRupees(amtNew));

            updateTransactionAndAdjust(nameNew, changedAmt);
        });
    }

    private void deleteTransactionAndAdjust() {
        DatabaseHelperTransaction dbt = new DatabaseHelperTransaction(this);
        dbt.open();
        int sendVal    = dbt.getSendValue(transactionId);
        int receiveVal = dbt.getReceiveValue(transactionId);
        dbt.deleteTransaction(transactionId);
        dbt.close();

        adjustCustomerBalance(sendVal, receiveVal, originalAmount, true);

        // return to the single-record screen
        startActivity(new Intent(this, SingleLedgerRecord.class)
                .putExtra("user_id", vendorId)
                .putExtra("customer_user_id", customerId)
                .putExtra("customer_name", customerName)
        );
        finish();
    }

    private void updateTransactionAndAdjust(String newName, int newAmount) {
        DatabaseHelperTransaction dbt = new DatabaseHelperTransaction(this);
        dbt.open();
        int sendVal    = dbt.getSendValue(transactionId);
        int receiveVal = dbt.getReceiveValue(transactionId);
        dbt.updateTransaction(transactionId, newName, newAmount);
        dbt.close();

        // first undo original, then apply new
        adjustCustomerBalance(sendVal, receiveVal, originalAmount, true);
        adjustCustomerBalance(sendVal, receiveVal, newAmount, false);

        startActivity(new Intent(this, SingleLedgerRecord.class)
                .putExtra("user_id", vendorId)
                .putExtra("customer_user_id", customerId)
                .putExtra("customer_name", customerName)
        );
        finish();
    }

    private void adjustCustomerBalance(int sendVal, int receiveVal, int amt, boolean undo) {
        DatabaseHelperCustomer dbc = new DatabaseHelperCustomer(this);
        dbc.open();
        int current = dbc.getRemainingAmountForCustomer(customerId);
        int updated;
        if (sendVal == 1) {
            updated = undo
                    ? current + amt
                    : current - amt;
        } else {
            updated = undo
                    ? current - amt
                    : current + amt;
        }
        dbc.updateCustomerRemainingAmount(customerId, updated);
        dbc.close();
    }

    private String getConvertedAmount(int amount) {
        double val;
        switch (selectedCurrency) {
            case "Dollar": val = amount / 278.05; break;
            case "Riyal":  val = amount / 74.13;  break;
            case "Yen":    val = amount / 1.82;   break;
            default:       val = amount;          break;
        }
        return String.format("%.2f", val);
    }

    private String getRupees(String s) {
        double val = Double.parseDouble(s);
        double rupees;
        switch (selectedCurrency) {
            case "Dollar": rupees = val * 278.05; break;
            case "Riyal":  rupees = val * 74.13;  break;
            case "Yen":    rupees = val * 1.82;   break;
            default:       rupees = val;          break;
        }
        return String.valueOf((int)Math.round(rupees));
    }
}
