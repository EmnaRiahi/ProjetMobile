package com.example.mama;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.api.OverpassResponse;

import java.util.List;

public class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder> {

    private List<OverpassResponse.Element> clinics;

    public ClinicAdapter(List<OverpassResponse.Element> clinics) {
        this.clinics = clinics;
    }

    @NonNull
    @Override
    public ClinicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clinic, parent, false);
        return new ClinicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClinicViewHolder holder, int position) {
        OverpassResponse.Element clinic = clinics.get(position);

        // 1. Affichage du Nom
        String clinicName = "Clinique inconnue";
        if (clinic.tags != null && clinic.tags.name != null) {
            clinicName = clinic.tags.name;
        }
        holder.tvName.setText(clinicName);

        // 2. Affichage du Téléphone
        if (clinic.tags != null && clinic.tags.phone != null) {
            holder.tvPhone.setText("📞 " + clinic.tags.phone);
        } else {
            holder.tvPhone.setText("📞 Non disponible");
        }

        // -----------------------------------------------------------
        // 3. LE CLIC MAGIQUE POUR LE TRAJET (Google Maps)
        // -----------------------------------------------------------
        String finalClinicName = clinicName; // Variable finale pour l'utilisation dans le clic
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupération des coordonnées de la clinique cliquée
                double lat = clinic.lat;
                double lon = clinic.lon;

                // Création de l'URI pour Google Maps Navigation
                // Format : google.navigation:q=latitude,longitude
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);

                // Création de l'Intent
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                // On essaie de lancer Maps
                // mapIntent.setPackage("com.google.android.apps.maps"); // Force Google Maps si tu veux

                Context context = v.getContext();
                try {
                    context.startActivity(mapIntent);
                } catch (Exception e) {
                    // Si aucune application de carte n'est installée (rare)
                    Toast.makeText(context, "Veuillez installer Google Maps", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (clinics != null) ? clinics.size() : 0;
    }

    public static class ClinicViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;

        public ClinicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvClinicName);
            tvPhone = itemView.findViewById(R.id.tvClinicPhone);
        }
    }
}