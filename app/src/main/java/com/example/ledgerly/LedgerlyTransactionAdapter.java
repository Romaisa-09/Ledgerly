package com.example.ledgerly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LedgerlyTransactionAdapter
        extends RecyclerView.Adapter<LedgerlyTransactionAdapter.ViewHolder> {

    private final ArrayList<Transaction> transactions;
    private final Context context;
    private final String selectedCurrency;
    private final ItemSelected parentActivity;
    private final int customerId;

    public interface ItemSelected {
        void onItemClicked(int index);
    }

    public LedgerlyTransactionAdapter(Context c,
                                      ArrayList<Transaction> list,
                                      SharedPreferences prefs,
                                      int customerId) {
        this.context = c;
        this.transactions = list;
        this.selectedCurrency = prefs.getString("selected_currency", "Rupees");
        this.parentActivity = (ItemSelected) c;
        this.customerId = customerId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ledger_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Transaction tx = transactions.get(pos);
        holder.itemView.setTag(tx);

        // coloring logic
        boolean isSend = tx.getSend() == 1;
        int color = isSend
                ? context.getColor(R.color.ledgerly_primary)
                : context.getColor(R.color.ledgerly_secondary);
        holder.tvAmount.setTextColor(color);
        holder.btnAction.setText(isSend ?
                context.getString(R.string.request) : context.getString(R.string.send));

        // set text
        holder.tvName.setText(tx.getName());
        holder.tvDate.setText(tx.getDate());
        holder.tvTime.setText(tx.getTime());
        holder.tvAmount.setText(formatAmount(tx.getAmount()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private String formatAmount(double amt) {
        switch (selectedCurrency) {
            case "Dollar": return "$ " + String.format("%.2f", amt / 278.05);
            case "Riyal":  return "SAR " + String.format("%.2f", amt / 74.13);
            case "Yen":    return "¥ " + String.format("%.2f", amt / 1.82);
            default:       return "PKR " + String.format("%.2f", amt);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvTime, tvAmount;
        Button   btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tvNameTransaction);
            tvDate   = itemView.findViewById(R.id.tvDateTransaction);
            tvTime   = itemView.findViewById(R.id.tvTimeTransaction);
            tvAmount = itemView.findViewById(R.id.tvAmountTransaction);
            btnAction= itemView.findViewById(R.id.btnSendReceiveTransaction);

            btnAction.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                Transaction tx = transactions.get(pos);
                if (tx.getSend() == 1) {
                    // SMS request
                    DatabaseHelperCustomer db = new DatabaseHelperCustomer(context);
                    db.open();
                    String phone = db.getPhoneNumber(customerId);
                    db.close();
                    Intent intent = new Intent(Intent.ACTION_SENDTO,
                            Uri.parse("smsto:" + phone));
                    intent.putExtra("sms_body",
                            "You owe: " + tx.getAmount() + " PKR");
                    context.startActivity(intent);
                } else {
                    // JazzCash flow
                    Intent jazz = context.getPackageManager()
                            .getLaunchIntentForPackage("com.techlogix.mobilinkcustomer");
                    if (jazz != null) context.startActivity(jazz);
                    else new AlertDialog.Builder(context)
                            .setTitle("JazzCash Not Installed")
                            .setMessage("Please install JazzCash to proceed.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            });

            itemView.setOnClickListener(v ->
                    parentActivity.onItemClicked(getAdapterPosition()));
        }
    }
}
