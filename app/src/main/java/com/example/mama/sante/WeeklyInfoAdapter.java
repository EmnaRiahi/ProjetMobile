package com.example.mama.sante;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.R;

import java.util.List;

public class WeeklyInfoAdapter extends RecyclerView.Adapter<WeeklyInfoAdapter.ViewHolder> {

    private final Context context;
    private final List<WeeklyInfo> weeklyInfoList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WeeklyInfo weeklyInfo);
    }

    private boolean isDarkMode = false;

    public WeeklyInfoAdapter(Context context, List<WeeklyInfo> weeklyInfoList, OnItemClickListener listener) {
        this.context = context;
        this.weeklyInfoList = weeklyInfoList;
        this.listener = listener;
    }

    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weekly_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeeklyInfo item = weeklyInfoList.get(position);
        holder.tvWeek.setText("S. " + item.getWeek());
        holder.tvTitle.setText(item.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));

        // Apply Theme
        if (isDarkMode) {
            ((com.google.android.material.card.MaterialCardView) holder.itemView)
                    .setCardBackgroundColor(
                            androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_card));
            holder.tvTitle.setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_text_primary));
        } else {
            ((com.google.android.material.card.MaterialCardView) holder.itemView)
                    .setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.white));
            holder.tvTitle.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_primary));
        }
    }

    @Override
    public int getItemCount() {
        return weeklyInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeek, tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeek = itemView.findViewById(R.id.tvWeek);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
