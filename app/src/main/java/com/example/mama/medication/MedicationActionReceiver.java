package com.example.mama.medication;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.mama.user.MyDatabaseHelper;

public class MedicationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String medId = intent.getStringExtra("med_id");
        String scheduledTime = intent.getStringExtra("scheduled_time");
        int notificationId = intent.getIntExtra("notification_id", -1);

        MyDatabaseHelper db = new MyDatabaseHelper(context);

        if ("ACTION_TAKEN".equals(action)) {
            db.logMedicationResponse(medId, "Taken", scheduledTime);
            Toast.makeText(context, "Médicament noté comme pris", Toast.LENGTH_SHORT).show();
        } else if ("ACTION_MISSED".equals(action)) {
            db.logMedicationResponse(medId, "Missed", scheduledTime);
            Toast.makeText(context, "Médicament noté comme non pris", Toast.LENGTH_SHORT).show();
        }

        // Fermer la notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && notificationId != -1) {
            notificationManager.cancel(notificationId);
        }
    }
}
