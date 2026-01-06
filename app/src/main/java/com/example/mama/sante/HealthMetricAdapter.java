package com.example.mama.sante;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.R;

import java.util.List;

public class HealthMetricAdapter extends RecyclerView.Adapter<HealthMetricAdapter.ViewHolder> {

    private final Context context;
    private final List<HealthMetric> metrics;
    private final OnItemClickListener listener;

    // Interface pour gérer les clics sur les boutons
    public interface OnItemClickListener {
        void onEditClick(HealthMetric metric);
        void onDeleteClick(HealthMetric metric);
    }

    public HealthMetricAdapter(Context context, List<HealthMetric> metrics, OnItemClickListener listener) {
        this.context = context;
        this.metrics = metrics;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_health_metric, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthMetric metric = metrics.get(position);

        holder.tvDate.setText("Date: " + metric.getDate());
        holder.tvWeight.setText("Poids: " + metric.getWeight() + " kg");
        holder.tvBloodPressure.setText("Tension: " + metric.getSystolic() + "/" + metric.getDiastolic() + " mmHg");

        // Gérer les clics sur les boutons
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(metric));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(metric));
    }

    @Override
    public int getItemCount() {
        return metrics.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeight, tvBloodPressure;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvBloodPressure = itemView.findViewById(R.id.tvBloodPressure);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
