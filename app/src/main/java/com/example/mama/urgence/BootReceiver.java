package com.example.mama.urgence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.example.mama.user.MyDatabaseHelper;
import com.example.mama.medication.AlarmHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarms(context);
        }
    }

    private void rescheduleAlarms(Context context) {
        MyDatabaseHelper db = new MyDatabaseHelper(context);
        Cursor cursor = db.getAllMedications();
        if (cursor != null) {
            AlarmHelper alarmHelper = new AlarmHelper(context);
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String dose = cursor.getString(4);
                String freq = cursor.getString(5);
                String schedule = cursor.getString(6);
                String time = cursor.getString(3);
                
                alarmHelper.scheduleMedicationAlarms(id, name, dose, freq, schedule, time);
            }
            cursor.close();
        }
    }
}
