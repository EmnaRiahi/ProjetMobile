package com.example.mama;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Calendar;

/**
 * This receiver is triggered when the device boots.
 * It reschedules all medication alarms so they work even after restart.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule all medication alarms from the database
            MyDatabaseHelper db = new MyDatabaseHelper(context);
            Cursor cursor = db.getAllMedications();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String medName = cursor.getString(1); // Name
                    String timeStr = cursor.getString(3); // Time (HH:mm)

                    if (timeStr != null && !timeStr.isEmpty()) {
                        scheduleAlarm(context, medName, timeStr);
                    }
                }
                cursor.close();
            }
        }
    }

    private void scheduleAlarm(Context context, String medName, String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // If time is in past today, set for tomorrow
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, MedicationAlarmReceiver.class);
            alarmIntent.putExtra("medName", medName);

            int alarmId = medName.hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, alarmId, alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
