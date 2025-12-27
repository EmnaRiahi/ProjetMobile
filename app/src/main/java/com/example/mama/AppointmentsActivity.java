package com.example.mama;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class AppointmentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab;
    MyDatabaseHelper myDB;
    ArrayList<Appointment> rdvList;
    AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assure-toi que le nom du layout ci-dessous correspond à ton fichier XML
        setContentView(R.layout.activity_appointments);

        recyclerView = findViewById(R.id.recyclerViewRdv);
        fab = findViewById(R.id.fabAdd);
        myDB = new MyDatabaseHelper(this);
        rdvList = new ArrayList<>();

        // 1. Charger les données depuis la base
        loadData();

        // 2. Configurer la liste
        adapter = new AppointmentAdapter(this, rdvList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Bouton Ajouter (+)
        fab.setOnClickListener(v -> showAddDialog());
    }

    void loadData() {
        rdvList.clear();
        Cursor cursor = myDB.getAllAppointments();
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "Aucun rendez-vous", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                // On récupère ID, TITRE, DATE, HEURE
                rdvList.add(new Appointment(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        }
    }

    void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_rdv, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etDialogTitle);
        EditText etDate = view.findViewById(R.id.etDialogDate);
        EditText etTime = view.findViewById(R.id.etDialogTime);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String t = etTitle.getText().toString();
            String d = etDate.getText().toString();
            String time = etTime.getText().toString();

            if (!t.isEmpty() && !d.isEmpty()) {
                myDB.addAppointment(t, d, time);
                loadData(); // Recharger la liste
                adapter.notifyDataSetChanged(); // Rafraichir l'écran
            } else {
                Toast.makeText(this, "Titre et Date requis", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }
}