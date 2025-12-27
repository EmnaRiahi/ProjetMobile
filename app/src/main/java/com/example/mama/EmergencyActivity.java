package com.example.mama;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.api.OverpassResponse;
import com.example.mama.api.OverpassService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EmergencyActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView tvStatus;

    // Client de localisation Google
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        // J'ai ajouté un TextView (optionnel) si tu veux afficher un message d'état,
        // sinon tu peux utiliser des Toast.

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1. Initialiser le client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 2. Vérifier les permissions et lancer la recherche
        checkPermissionAndSearch();
    }

    private void checkPermissionAndSearch() {
        // Vérifie si on a la permission FINE_LOCATION (GPS Précis)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Si non, on demande la permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Si oui, on récupère la position
            getCurrentLocation();
        }
    }

    // Gestion de la réponse de l'utilisateur (Accepter/Refuser le GPS)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission GPS refusée. Impossible de localiser.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void getCurrentLocation() {
        // Vérification de sécurité requise par Android Studio
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Récupère la dernière position connue
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // On a la position ! On lance l'API avec les vraies coordonnées
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();

                            // Petit message pour confirmer
                            Toast.makeText(EmergencyActivity.this, "Position trouvée, recherche en cours...", Toast.LENGTH_SHORT).show();

                            searchHospitals(lat, lon);
                        } else {
                            // Cas rare : GPS activé mais pas de position enregistrée
                            Toast.makeText(EmergencyActivity.this, "Impossible de récupérer la position GPS. Vérifiez que la localisation est activée.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    // Méthode modifiée pour accepter lat/lon en paramètres
    private void searchHospitals(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://overpass-api.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OverpassService service = retrofit.create(OverpassService.class);

        // Rayon de recherche : 5000 mètres (5km) autour de MA position
        String query = "[out:json];node(around:5000," + lat + "," + lon + ")[amenity~\"hospital|clinic\"];out;";

        service.getNearbyHospitals(query).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<OverpassResponse.Element> results = response.body().elements;

                    if (results.isEmpty()) {
                        Toast.makeText(EmergencyActivity.this, "Aucune clinique trouvée à 5km de votre position.", Toast.LENGTH_LONG).show();
                    } else {
                        ClinicAdapter adapter = new ClinicAdapter(results);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(EmergencyActivity.this, "Erreur API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EmergencyActivity.this, "Erreur Réseau : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}