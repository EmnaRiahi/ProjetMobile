package com.example.mama.sport;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mama.R;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {

    private List<ActiviteEntity> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ActiviteEntity activity); // Pour Update
        void onItemLongClick(ActiviteEntity activity); // Pour Delete
    }

    public HistoryAdapter(List<ActiviteEntity> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        ActiviteEntity a = list.get(pos);
        h.txtDate.setText(a.date);
        h.txtSteps.setText(a.steps + " steps");
        h.txtDetails.setText(a.duration + " mins • " + a.type);

        if ("INDOOR".equalsIgnoreCase(a.type)) {
            h.imgIcon.setImageResource(android.R.drawable.ic_menu_mylocation);
        } else {
            h.imgIcon.setImageResource(android.R.drawable.ic_menu_directions);
        }

        if (a.isAchieved) {
            h.cardView.setCardBackgroundColor(0xFFD5F5E3); // Light Green
        } else {
            h.cardView.setCardBackgroundColor(0xFFFFFFFF); // White
        }

        // Clic simple pour modifier
        h.itemView.setOnClickListener(v -> listener.onItemClick(a));

        // Clic long pour supprimer
        h.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(a);
            return true;
        });
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class Holder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardView;
        TextView txtDate, txtSteps, txtDetails;
        ImageView imgIcon;
        Holder(View v) {
            super(v);
            cardView = (com.google.android.material.card.MaterialCardView) v;
            txtDate = v.findViewById(R.id.txtItemDate);
            txtSteps = v.findViewById(R.id.txtItemSteps);
            txtDetails = v.findViewById(R.id.txtItemDetails);
            imgIcon = v.findViewById(R.id.imgTypeIcon);
        }
    }
}