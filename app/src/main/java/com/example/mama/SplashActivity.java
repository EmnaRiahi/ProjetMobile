package com.example.mama;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Durée du splash screen (3000ms = 3 secondes)
    private static int SPLASH_SCREEN = 3000;

    // Variables pour l'animation
    Animation topAnim;
    ImageView image;
    TextView logo, slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enlever la barre de statut pour le plein écran
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        // Charger l'animation
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);

        // Hooks (Lier le code au XML)
        image = findViewById(R.id.logo_splash);
        logo = findViewById(R.id.tv_splash);

        // Lancer l'animation sur l'image et le texte
        image.setAnimation(topAnim);
        logo.setAnimation(topAnim);

        // Passer à l'activité suivante après 3 secondes
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Empêche de revenir au splash avec le bouton retour
            }
        }, SPLASH_SCREEN);
    }
}