package com.example.mama.medication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.mama.R;

public class MedicationAlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "MEDICATION_REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medId = intent.getStringExtra("med_id");
        String medName = intent.getStringExtra("med_name");
        String medDose = intent.getStringExtra("med_dose");
        String scheduledTime = intent.getStringExtra("scheduled_time");

        createNotificationChannel(context);

        // Action "Pris" (Yes)
        Intent takenIntent = new Intent(context, MedicationActionReceiver.class);
        takenIntent.setAction("ACTION_TAKEN");
        takenIntent.putExtra("med_id", medId);
        takenIntent.putExtra("scheduled_time", scheduledTime);
        takenIntent.putExtra("notification_id", medId.hashCode());
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(context, medId.hashCode() + 1, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Action "Non pris" (No)
        Intent missedIntent = new Intent(context, MedicationActionReceiver.class);
        missedIntent.setAction("ACTION_MISSED");
        missedIntent.putExtra("med_id", medId);
        missedIntent.putExtra("scheduled_time", scheduledTime);
        missedIntent.putExtra("notification_id", medId.hashCode());
        PendingIntent missedPendingIntent = PendingIntent.getBroadcast(context, medId.hashCode() + 2, missedIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Rappel Médicament : " + medName)
                .setContentText("Il est temps de prendre " + medDose + ".")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.checkbox_on_background, "Oui - Je l'ai pris", takenPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Non - Pas encore", missedPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(medId.hashCode(), builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Rappels Médicaments";
            String description = "Canal pour les rappels de prise de médicaments";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
