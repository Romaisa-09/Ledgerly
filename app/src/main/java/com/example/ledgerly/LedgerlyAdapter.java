package com.example.ledgerly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class LedgerlyAdapter extends RecyclerView.Adapter<LedgerlyAdapter.ViewHolder> {

    public interface ItemSelected {
        void onItemClicked(int index);
    }

    private final ArrayList<Customer> customers;
    private final Context context;
    private final String selectedCurrency;
    private final ItemSelected parentActivity;

    public LedgerlyAdapter(Context context,
                           ArrayList<Customer> list,
                           SharedPreferences prefs) {
        this.context = context;
        this.customers = list;
        this.selectedCurrency = prefs.getString("selected_currency", "Rupees");
        this.parentActivity = (ItemSelected) context;
    }

    @NonNull
    @Override
    public LedgerlyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ledger_record, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LedgerlyAdapter.ViewHolder holder, int pos) {
        Customer c = customers.get(pos);
        holder.itemView.setTag(c);

        boolean positive = c.getRemainingAmount() >= 0;
        int color = positive
                ? context.getColor(R.color.ledgerly_primary)
                : context.getColor(R.color.ledgerly_secondary);
        holder.tvRemaining.setTextColor(color);
        holder.btnAction.setText(positive
                ? context.getString(R.string.request)
                : context.getString(R.string.send));

        holder.tvName.setText(c.getName());
        holder.tvDate.setText(c.getDate());
        holder.tvTime.setText(c.getTime());
        holder.tvRemaining.setText(formatAmount(Math.abs(c.getRemainingAmount())));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    private String formatAmount(double amt) {
        switch (selectedCurrency) {
            case "Dollar":
                return String.format(Locale.US, "$ %.2f", amt / 278.05);
            case "Riyal":
                return String.format(Locale.US, "SAR %.2f", amt / 74.13);
            case "Yen":
                return String.format(Locale.US, "¥ %.2f", amt / 1.82);
            default:
                return String.format(Locale.US, "PKR %.2f", amt);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvTime, tvRemaining;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvName);
            tvDate      = itemView.findViewById(R.id.tvDate);
            tvTime      = itemView.findViewById(R.id.tvTime);
            tvRemaining = itemView.findViewById(R.id.tvRemainingAmount);
            btnAction   = itemView.findViewById(R.id.btnSendReceiveCustomer);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int pos = holder.getAdapterPosition();
        Customer c = customers.get(pos);

        holder.btnAction.setOnClickListener(v -> {
            if (c.getRemainingAmount() >= 0) {
                String phone = c.getPhoneNumber();
                Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
                sms.putExtra("sms_body", "You owe: " + c.getRemainingAmount() + " PKR");
                context.startActivity(sms);
            } else {
                Intent jazz = context.getPackageManager()
                        .getLaunchIntentForPackage("com.techlogix.mobilinkcustomer");
                if (jazz != null) context.startActivity(jazz);
                else new AlertDialog.Builder(context)
                        .setTitle(R.string.jazzcash_not_installed)
                        .setMessage(R.string.install_jazzcash_prompt)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });

        holder.itemView.setOnClickListener(v ->
                parentActivity.onItemClicked(pos)
        );

        holder.itemView.setOnLongClickListener(v -> {
            showUpdateDeleteDialog(c, pos);
            return true;
        });
    }

    private void showUpdateDeleteDialog(Customer customer, int pos) {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_update_delete_ledgerly_customer, null);
        EditText etName  = dialogView.findViewById(R.id.etNameDialog);
        EditText etPhone = dialogView.findViewById(R.id.etPhoneDialog);
        etName.setText(customer.getName());
        etPhone.setText(customer.getPhoneNumber());

        new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(R.string.update, (d, id) -> {
                    String newName = etName.getText().toString();
                    String newPhone= etPhone.getText().toString();
                    DatabaseHelperCustomer db = new DatabaseHelperCustomer(context);
                    db.open();
                    db.updateCustomer(customer.getCid(), newName, newPhone);
                    db.close();
                    customer.setName(newName);
                    customer.setPhoneNumber(newPhone);
                    notifyItemChanged(pos);
                })
                .setNegativeButton(R.string.delete, (d, id) -> {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.delete_confirmation)
                            .setMessage(R.string.delete_prompt)
                            .setPositiveButton(R.string.yes, (dd, which) -> {
                                DatabaseHelperCustomer db = new DatabaseHelperCustomer(context);
                                db.open();
                                db.deleteCustomer(customer.getCid());
                                db.close();
                                customers.remove(pos);
                                notifyDataSetChanged();
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                })
                .show();
    }
}
