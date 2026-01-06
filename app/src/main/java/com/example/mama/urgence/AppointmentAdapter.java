package com.example.mama.urgence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.user.MyDatabaseHelper;
import com.example.mama.R;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.MyViewHolder> {

    Context context;
    List<Appointment> list;
    MyDatabaseHelper myDB;
    OnItemClickListener listener; // Pour gérer le clic de modification

    // Interface pour communiquer avec l'activité
    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public AppointmentAdapter(Context context, List<Appointment> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        myDB = new MyDatabaseHelper(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Appointment rdv = list.get(position);
        holder.title.setText(rdv.title);
        holder.date.setText(rdv.date + " à " + rdv.time);

        // 1. Clic sur la carte entière -> MODIFIER
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(rdv); // On envoie le RDV à l'activité
        });

        // 2. Clic sur la poubelle -> SUPPRIMER
        holder.btnDelete.setOnClickListener(v -> {
            myDB.deleteAppointment(rdv.id);
            list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        ImageButton btnDelete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvRdvTitle);
            date = itemView.findViewById(R.id.tvRdvDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteRdv);
        }
    }
}