package com.example.loom_group_2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.TripLog;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private List<TripLog> logList;
    private OnLogDeleteListener deleteListener;
    private OnItemLongClickListener longClickListener;

    public interface OnLogDeleteListener {
        void onLogDelete(TripLog log, int position);
    }

    public interface OnItemLongClickListener {
        void onLongClick(TripLog log);
    }

    public LogAdapter(List<TripLog> logList) { 
        this.logList = logList; 
    }

    public void setOnLogDeleteListener(OnLogDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        TripLog log = logList.get(position);
        holder.tvDate.setText(log.getDate());
        holder.tvTitle.setText(log.getTitle());
        holder.tvTime.setText("Time: " + log.getTime());
        holder.tvFuel.setText("Fuel: " + log.getFuel());
        holder.tvDistance.setText("Dist: " + log.getDistance());

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onLogDelete(log, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(log);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() { return logList.size(); }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvTime, tvFuel, tvDistance;
        ImageButton btnDelete;
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvFuel = itemView.findViewById(R.id.tvFuel);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnDelete = itemView.findViewById(R.id.btnDeleteLog);
        }
    }
}
