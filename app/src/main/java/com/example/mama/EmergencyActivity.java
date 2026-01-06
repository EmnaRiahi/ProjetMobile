package com.example.mama;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    FloatingActionButton fabCall; // Bouton appel urgence

    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Rayon de recherche élargi à 15km (15000 mètres)
    private static final int SEARCH_RADIUS = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        // Ajoute ce bouton dans ton XML si tu veux l'appel direct (voir étape suivante)
        fabCall = findViewById(R.id.fabCallEmergency);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkPermissionAndSearch();

        // Clic sur le bouton d'appel SAMU (190 en Tunisie, 15 en France, etc.)
        if(fabCall != null) {
            fabCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:190")); // SAMU
                startActivity(intent);
            });
        }
    }

    private void checkPermissionAndSearch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                searchHospitals(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "Position introuvable. Ouverture de Google Maps...", Toast.LENGTH_LONG).show();
                // Si pas de GPS, on ouvre Maps directement
                openGoogleMapsFallback();
            }
        });
    }

    private void searchHospitals(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://overpass-api.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OverpassService service = retrofit.create(OverpassService.class);

        // Requête élargie : Rayon 15km, Timeout augmenté [timeout:25]
        String query = String.format(java.util.Locale.US,
                "[out:json][timeout:25];node(around:%d,%f,%f)[amenity~\"hospital|clinic|pharmacy\"];out;",
                SEARCH_RADIUS, lat, lon);

        service.getNearbyHospitals(query).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<OverpassResponse.Element> results = response.body().elements;
                    if (results.isEmpty()) {
                        // Pas de résultats dans l'API -> Plan B
                        Toast.makeText(EmergencyActivity.this, "Aucune clinique détectée ici. Recherche sur Maps...", Toast.LENGTH_LONG).show();
                        openGoogleMapsFallback();
                    } else {
                        ClinicAdapter adapter = new ClinicAdapter(results);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    // Erreur API (Surcharge) -> Plan B
                    openGoogleMapsFallback();
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                // Erreur Réseau -> Plan B
                Toast.makeText(EmergencyActivity.this, "Problème réseau. Ouverture de Maps...", Toast.LENGTH_SHORT).show();
                openGoogleMapsFallback();
            }
        });
    }

    // --- LE PLAN B (Sauve la vie !) ---
    private void openGoogleMapsFallback() {
        // Lance une recherche générique "Hôpital" autour de la position actuelle via l'appli Maps
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=hopital+clinique");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
            finish(); // On ferme l'activité urgence pour laisser place à Maps
        } catch (Exception e) {
            Toast.makeText(this, "Installez Google Maps pour plus de sécurité", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}