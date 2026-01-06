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

    private boolean isDarkMode = false;

    public HealthMetricAdapter(Context context, List<HealthMetric> metrics, OnItemClickListener listener) {
        this.context = context;
        this.metrics = metrics;
        this.listener = listener;
    }

    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        notifyDataSetChanged();
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

        holder.tvDate.setText("📅 " + metric.getDate());
        holder.tvWeight.setText(metric.getWeight() + " kg");
        holder.tvBloodPressure.setText(metric.getSystolic() + "/" + metric.getDiastolic());

        // Gérer les clics sur les boutons
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(metric));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(metric));

        // Apply Theme
        applyThemeToStringViewHolder(holder);
    }

    private void applyThemeToStringViewHolder(ViewHolder holder) {
        if (isDarkMode) {
            ((com.google.android.material.card.MaterialCardView) holder.itemView)
                    .setCardBackgroundColor(
                            androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_card));
            holder.tvDate.setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_text_primary));
            holder.tvWeight.setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_text_primary));
            holder.tvBloodPressure.setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_text_primary));
        } else {
            ((com.google.android.material.card.MaterialCardView) holder.itemView)
                    .setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.white));
            holder.tvDate.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_primary));
            holder.tvWeight.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_primary));
            holder.tvBloodPressure
                    .setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_primary));
        }
    }

    @Override
    public int getItemCount() {
        return metrics.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeight, tvBloodPressure;
        android.widget.ImageButton btnEdit, btnDelete;

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
