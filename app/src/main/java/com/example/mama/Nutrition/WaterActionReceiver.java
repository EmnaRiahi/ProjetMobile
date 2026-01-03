package com.example.mama.Nutrition;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class WaterActionReceiver extends BroadcastReceiver {

    private static final int WATER_NOTIFICATION_ID = 2002;
    private static final float WATER_AMOUNT = 0.4f; // 400ml = 0.4L

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Fermer la notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(WATER_NOTIFICATION_ID);

        if ("ACTION_YES".equals(action)) {
            // Ajouter 0.4L d'eau
            addWater(context, WATER_AMOUNT);
            Toast.makeText(context, "💧 +0.4L ajouté !", Toast.LENGTH_SHORT).show();
        } else if ("ACTION_NO".equals(action)) {
            // Ne rien faire, juste fermer la notification
            Toast.makeText(context, "D'accord, à plus tard !", Toast.LENGTH_SHORT).show();
        }

        // Réinitialiser le cooldown du service
        resetServiceCooldown(context);
    }

    private void addWater(Context context, float amount) {
        // Utiliser la méthode statique de WaterTrackerActivity
        float currentIntake = WaterTrackerActivity.getCurrentWaterIntake(context);
        float newIntake = currentIntake + amount;

        // Sauvegarder
        context.getSharedPreferences("WaterTrackerPrefs", Context.MODE_PRIVATE)
                .edit()
                .putFloat("water_intake", newIntake)
                .apply();
    }

    private void resetServiceCooldown(Context context) {
        // Envoyer un broadcast au service pour réinitialiser le cooldown
        Intent resetIntent = new Intent(context, WaterReminderService.class);
        resetIntent.setAction("RESET_COOLDOWN");
        context.startService(resetIntent);
    }
}