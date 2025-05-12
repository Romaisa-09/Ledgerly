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
import java.util.List;
import java.util.Locale;

public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface ItemSelected {
        void onItemClicked(int index);
    }

    private final List<Transaction> transactions;
    private final Context context;
    private final String selectedCurrency;
    private final ItemSelected parentActivity;
    private final int customerId;

    public TransactionAdapter(Context c,
                              List<Transaction> list,
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
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Transaction tx = transactions.get(position);

        holder.tvName.setText(tx.getName());
        holder.tvDate.setText(tx.getDate());
        holder.tvTime.setText(tx.getTime());
        holder.tvAmount.setText(formatAmount(tx.getAmount()));

        boolean isSend = tx.getSendFlag() == 1;
        int color = isSend
                ? context.getColor(R.color.ledgerly_primary)
                : context.getColor(R.color.ledgerly_secondary);
        holder.tvAmount.setTextColor(color);
        holder.btnAction.setText(isSend
                ? context.getString(R.string.request)
                : context.getString(R.string.send));

        holder.btnAction.setOnClickListener(v -> {
            if (isSend) {
                // Send SMS
                DatabaseHelperCustomer db = new DatabaseHelperCustomer(context);
                db.open();
                String phone = db.getPhoneNumber(customerId);
                db.close();
                Intent sms = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + phone));
                sms.putExtra("sms_body", "You owe: " + tx.getAmount() + " PKR");
                context.startActivity(sms);
            } else {
                // JazzCash
                Intent jazz = context.getPackageManager()
                        .getLaunchIntentForPackage("com.techlogix.mobilinkcustomer");
                if (jazz != null) {
                    context.startActivity(jazz);
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("JazzCash Not Installed")
                            .setMessage("Please install JazzCash to proceed.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        });

        holder.itemView.setOnClickListener(v ->
                parentActivity.onItemClicked(holder.getAdapterPosition())
        );
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private String formatAmount(double amt) {
        switch (selectedCurrency) {
            case "Dollar":
                return String.format(Locale.getDefault(), "$ %.2f", amt / 278.05);
            case "Riyal":
                return String.format(Locale.getDefault(), "SAR %.2f", amt / 74.13);
            case "Yen":
                return String.format(Locale.getDefault(), "¥ %.2f", amt / 1.82);
            default:
                return String.format(Locale.getDefault(), "PKR %.2f", amt);
        }
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        final TextView tvName, tvDate, tvTime, tvAmount;
        final Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvNameTransaction);
            tvDate    = itemView.findViewById(R.id.tvDateTransaction);
            tvTime    = itemView.findViewById(R.id.tvTimeTransaction);
            tvAmount  = itemView.findViewById(R.id.tvAmountTransaction);
            btnAction = itemView.findViewById(R.id.btnSendReceiveTransaction);
        }
    }
}
