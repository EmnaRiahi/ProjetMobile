package com.example.mama.Nutrition;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class WaterReminderService extends Service implements SensorEventListener {

    private static final String TAG = "WaterReminderService";
    private static final String CHANNEL_ID = "WaterReminderChannel";
    private static final int NOTIFICATION_ID = 2001;
    private static final int WATER_NOTIFICATION_ID = 2002;

    // Paramètres de détection
    private static final float Z_THRESHOLD = 2.0f; // Seuil de mouvement sur l'axe Z
    private static final long COOLDOWN_PERIOD = 30000; // 3 minutes en millisecondes

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastZ = 0f;
    private boolean isPhonePickedUp = false;
    private long lastNotificationTime = 0;
    private boolean canSendNotification = true;

    @Override
    public void onCreate() {
        super.onCreate();

        // Créer le canal de notification
        createNotificationChannel();

        // Initialiser le capteur
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        // Démarrer en tant que service foreground
        startForeground(NOTIFICATION_ID, createForegroundNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2];
            float deltaZ = Math.abs(z - lastZ);

            if (deltaZ > Z_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastNotificationTime > COOLDOWN_PERIOD) {
                    sendWaterReminder();
                    lastNotificationTime = currentTime;
                }
            }

            lastZ = z;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Rappels pour boire de l'eau");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createForegroundNotification() {
        Intent notificationIntent = new Intent(this, WaterTrackerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Water Tracker")
                .setContentText("Service de rappel d'eau actif")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void sendWaterReminder() {
        // Intent pour le bouton YES
        Intent yesIntent = new Intent(this, WaterActionReceiver.class);
        yesIntent.setAction("ACTION_YES");
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                yesIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent pour le bouton NO
        Intent noIntent = new Intent(this, WaterActionReceiver.class);
        noIntent.setAction("ACTION_NO");
        PendingIntent noPendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                noIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent pour ouvrir l'application
        Intent openIntent = new Intent(this, WaterTrackerActivity.class);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                this,
                2,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Créer la notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("💧 Hydratation")
                .setContentText("Avez-vous bu de l'eau ?")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent)
                .addAction(android.R.drawable.ic_input_add, "YES", yesPendingIntent)
                .addAction(android.R.drawable.ic_delete, "NO", noPendingIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(WATER_NOTIFICATION_ID, notification);
    }

    public void resetNotificationCooldown() {
        canSendNotification = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WaterReminderService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}