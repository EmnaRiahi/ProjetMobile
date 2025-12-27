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

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.MyViewHolder> {

    Context context;
    List<Appointment> list;
    MyDatabaseHelper myDB;

    public AppointmentAdapter(Context context, List<Appointment> list) {
        this.context = context;
        this.list = list;
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

        // Gestion du clic sur la poubelle (Suppression)
        holder.btnDelete.setOnClickListener(v -> {
            myDB.deleteAppointment(rdv.id); // Supprime de la base
            list.remove(position);          // Supprime de l'écran
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