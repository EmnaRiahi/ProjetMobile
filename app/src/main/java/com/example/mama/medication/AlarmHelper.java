package com.example.mama.medication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class AlarmHelper {
    private Context context;

    public AlarmHelper(Context context) {
        this.context = context;
    }

    public void scheduleMedicationAlarms(String medId, String name, String dose, String freq, String schedule, String customTime) {
        if (schedule != null) {
            if (schedule.contains("Matin")) scheduleAlarm(medId, name, dose, 8, 0, 0);
            if (schedule.contains("Midi")) scheduleAlarm(medId, name, dose, 12, 0, 1);
            if (schedule.contains("Soir")) scheduleAlarm(medId, name, dose, 18, 0, 2);
            if (schedule.contains("Nuit")) scheduleAlarm(medId, name, dose, 22, 0, 3);
        }

        if (customTime != null && customTime.contains(":")) {
            String[] parts = customTime.split(":");
            if (parts.length == 2) {
                try {
                    int h = Integer.parseInt(parts[0].trim());
                    int m = Integer.parseInt(parts[1].trim());
                    scheduleAlarm(medId, name, dose, h, m, 4);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public void cancelMedicationAlarms(String medId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        for (int i = 0; i < 5; i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, medId.hashCode() + i, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private void scheduleAlarm(String medId, String name, String dose, int hour, int minute, int requestCodeOffset) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.putExtra("med_id", medId);
        intent.putExtra("med_name", name);
        intent.putExtra("med_dose", dose);
        intent.putExtra("scheduled_time", String.format("%02d:%02d", hour, minute));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, medId.hashCode() + requestCodeOffset, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    // Fallback to inexact if permission not granted
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}
