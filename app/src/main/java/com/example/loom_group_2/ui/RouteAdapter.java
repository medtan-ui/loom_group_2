package com.example.loom_group_2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.RouteModel;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private List<RouteModel> routeList;
    private int selectedPosition = 0; // Default first route selected

    public RouteAdapter(List<RouteModel> routeList) {
        this.routeList = routeList;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        RouteModel route = routeList.get(position);

        holder.tvName.setText(route.getName());
        holder.tvTime.setText(route.getDurationText());
        holder.tvFuel.setText(route.getFuelText());

        // Update background based on selector (Blue/Yellow)
        holder.container.setSelected(selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() { return routeList.size(); }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvFuel;
        LinearLayout container;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRouteName);
            tvTime = itemView.findViewById(R.id.tvRouteTime);
            tvFuel = itemView.findViewById(R.id.tvRouteFuel);
            container = itemView.findViewById(R.id.routeContainer);
        }
    }
}}