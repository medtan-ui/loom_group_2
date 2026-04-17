package com.example.loom_group_2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FuelRefill;
import java.util.List;
import java.util.Locale;

public class FuelRefillAdapter extends RecyclerView.Adapter<FuelRefillAdapter.ViewHolder> {

    private List<FuelRefill> refillList;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(FuelRefill refill);
    }

    public FuelRefillAdapter(List<FuelRefill> refillList) {
        this.refillList = refillList;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setRefills(List<FuelRefill> refills) {
        this.refillList = refills;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fuel_refill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FuelRefill refill = refillList.get(position);
        holder.tvRefillDate.setText(refill.getDate());
        holder.tvLiters.setText(String.format(Locale.US, "%.2f liters added", refill.getLitersAdded()));
        holder.tvKpl.setText(String.format(Locale.US, "Real KPL: %.2f km/L", refill.getCalculatedKpl()));
        
        if (refill.getNotes() != null && !refill.getNotes().isEmpty()) {
            holder.tvRefillNotes.setText(refill.getNotes());
            holder.tvRefillNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvRefillNotes.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(refill);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return refillList != null ? refillList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRefillDate, tvLiters, tvKpl, tvRefillNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRefillDate = itemView.findViewById(R.id.tvRefillDate);
            tvLiters = itemView.findViewById(R.id.tvLiters);
            tvKpl = itemView.findViewById(R.id.tvKpl);
            tvRefillNotes = itemView.findViewById(R.id.tvRefillNotes);
        }
    }
}
