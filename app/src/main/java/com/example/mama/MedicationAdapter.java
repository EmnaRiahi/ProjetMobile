package com.example.mama;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    Context context;
    List<Medication> list;
    MyDatabaseHelper myDB;
    OnItemClickListener listener; // Pour le clic de modification

    public interface OnItemClickListener {
        void onItemClick(Medication med);
    }

    public MedicationAdapter(Context context, List<Medication> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        myDB = new MyDatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_med, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication med = list.get(position);
        holder.name.setText(med.name);
        holder.generic.setText(med.generic);
        holder.time.setText("⏰ " + med.time);
        holder.doses.setText("💊 " + med.doses); // Affichage doses

        // CLIC SUR LA CARTE -> MODIFIER
        holder.itemView.setOnClickListener(v -> listener.onItemClick(med));

        // CLIC POUBELLE -> SUPPRIMER
        holder.btnDelete.setOnClickListener(v -> {
            myDB.deleteMedication(med.id);
            list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());
        });

        // CLIC EDITER -> MODIFIER
        holder.btnEdit.setOnClickListener(v -> {
            listener.onItemClick(med);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, generic, time, doses;
        ImageButton btnDelete, btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvMedName);
            generic = itemView.findViewById(R.id.tvMedGeneric);
            time = itemView.findViewById(R.id.tvMedTime);
            doses = itemView.findViewById(R.id.tvMedDoses);
            btnDelete = itemView.findViewById(R.id.btnDeleteMed);
            btnEdit = itemView.findViewById(R.id.btnEditMed);
        }
    }
}