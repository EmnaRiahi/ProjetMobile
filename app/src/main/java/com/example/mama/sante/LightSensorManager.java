package com.example.mama.sante;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Utility class to manage light sensor for automatic dark mode switching
 * Designed specifically for the Santé (Health) section
 */
public class LightSensorManager implements SensorEventListener {

    private static final float DARK_THRESHOLD_LUX = 40.0f; // Below this = dark mode
    private static final float LIGHT_THRESHOLD_LUX = 60.0f; // Above this = light mode (hysteresis)

    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private ThemeChangeListener listener;
    private boolean isDarkMode = false;

    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDarkMode);
    }

    public LightSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) : null;
    }

    /**
     * Set the listener for theme change callbacks
     */
    public void setThemeChangeListener(ThemeChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Register the light sensor listener
     * Call this in onResume()
     */
    public void register() {
        if (sensorManager != null && lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Unregister the light sensor listener
     * Call this in onPause()
     */
    public void unregister() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Check if light sensor is available on this device
     */
    public boolean isAvailable() {
        return lightSensor != null;
    }

    /**
     * Get current dark mode state
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];

            // Use hysteresis to prevent flickering between modes
            boolean shouldBeDark;
            if (isDarkMode) {
                // If currently dark, need brighter light to switch to light mode
                shouldBeDark = lux < LIGHT_THRESHOLD_LUX;
            } else {
                // If currently light, need darker environment to switch to dark mode
                shouldBeDark = lux < DARK_THRESHOLD_LUX;
            }

            // Only notify if mode actually changed
            if (shouldBeDark != isDarkMode) {
                isDarkMode = shouldBeDark;
                if (listener != null) {
                    listener.onThemeChanged(isDarkMode);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }
}
