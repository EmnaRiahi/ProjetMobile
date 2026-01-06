package com.example.mama.medication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.user.MyDatabaseHelper;
import com.example.mama.R;

import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableActivity.ScheduledMedication> list;
    private Context context;
    private MyDatabaseHelper myDB;

    public TimetableAdapter(List<TimetableActivity.ScheduledMedication> list, Context context) {
        this.list = list;
        this.context = context;
        this.myDB = new MyDatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableActivity.ScheduledMedication item = list.get(position);
        holder.time.setText(item.time);
        holder.name.setText(item.name);
        holder.dose.setText(item.dose + " (" + item.slot + ")");

        // Check medication status today
        String status = myDB.getMedicationStatusToday(item.medId, item.time);
        
        if ("Taken".equals(status)) {
            // GREEN: Success
            holder.status.setImageResource(android.R.drawable.presence_online);
            holder.status.setColorFilter(Color.parseColor("#2ECC71"));
        } else {
            // Check if time has passed
            if (isTimePassed(item.time)) {
                // RED: Failed/Missed
                holder.status.setImageResource(android.R.drawable.presence_busy);
                holder.status.setColorFilter(Color.parseColor("#E74C3C"));
            } else {
                // GRAY: Future/Pending
                holder.status.setImageResource(android.R.drawable.presence_invisible);
                holder.status.setColorFilter(Color.parseColor("#D1D8E0"));
            }
        }
    }

    private boolean isTimePassed(String scheduledTime) {
        try {
            String[] parts = scheduledTime.split(":");
            int sHour = Integer.parseInt(parts[0]);
            int sMin = Integer.parseInt(parts[1]);

            java.util.Calendar now = java.util.Calendar.getInstance();
            int currentHour = now.get(java.util.Calendar.HOUR_OF_DAY);
            int currentMin = now.get(java.util.Calendar.MINUTE);

            if (currentHour > sHour) return true;
            if (currentHour == sHour && currentMin > sMin) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView time, name, dose;
        ImageView status;
        public ViewHolder(@NonNull View view) {
            super(view);
            time = view.findViewById(R.id.tvTime);
            name = view.findViewById(R.id.tvMedName);
            dose = view.findViewById(R.id.tvMedDose);
            status = view.findViewById(R.id.ivStatus);
        }
    }
}
