package com.example.mama;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class MedicationAlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "medication_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("MedAlarm", "🔔 onReceive triggered!");

        String medName = intent.getStringExtra("medName");
        if (medName == null)
            medName = "Médicament";

        android.util.Log.d("MedAlarm", "Medication: " + medName);

        // Create Notification Manager
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Rappels Médicaments",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Canal pour les rappels de prise de médicaments");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Tap Action (Open App)
        Intent tapIntent = new Intent(context, MedicationActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Use a system icon for now
                .setContentTitle("Rappel Médicament 💊")
                .setContentText("Il est l'heure de prendre votre : " + medName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[] { 0, 500, 200, 500 })
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        // Show
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        // --- RESCHEDULE FOR NEXT DAY (Daily Repeating) ---
        rescheduleForNextDay(context, medName);
    }

    /**
     * Reschedules the alarm for the same time tomorrow.
     * This makes daily reminders persistent.
     */
    private void rescheduleForNextDay(Context context, String medName) {
        try {
            // Fetch time from database for this med (ideally) or use a default
            // For simplicity, we re-fetch. Alternative: pass time in intent.
            MyDatabaseHelper db = new MyDatabaseHelper(context);
            android.database.Cursor cursor = db.getAllMedications();
            String timeStr = null;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (medName.equals(cursor.getString(1))) {
                        timeStr = cursor.getString(3);
                        break;
                    }
                }
                cursor.close();
            }

            if (timeStr != null) {
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.add(java.util.Calendar.DATE, 1); // Tomorrow
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
                calendar.set(java.util.Calendar.MINUTE, minute);
                calendar.set(java.util.Calendar.SECOND, 0);

                android.app.AlarmManager alarmManager = (android.app.AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);
                android.content.Intent alarmIntent = new android.content.Intent(context, MedicationAlarmReceiver.class);
                alarmIntent.putExtra("medName", medName);

                int alarmId = medName.hashCode();
                android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                        context, alarmId, alarmIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

                if (alarmManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
