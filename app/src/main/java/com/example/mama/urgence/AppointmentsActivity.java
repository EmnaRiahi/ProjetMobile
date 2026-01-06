package com.example.mama.urgence;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.user.MyDatabaseHelper;
import com.example.mama.user.NotificationReceiver;
import com.example.mama.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppointmentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab;
    MyDatabaseHelper myDB;
    ArrayList<Appointment> rdvList;
    AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        recyclerView = findViewById(R.id.recyclerViewRdv);
        fab = findViewById(R.id.fabAdd);
        myDB = new MyDatabaseHelper(this);
        rdvList = new ArrayList<>();

        loadData();

        adapter = new AppointmentAdapter(this, rdvList, appointment -> showAppointmentDialog(appointment));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> showAppointmentDialog(null));
    }

    void loadData() {
        rdvList.clear();
        Cursor cursor = myDB.getAllAppointments();
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "Aucun rendez-vous", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                rdvList.add(new Appointment(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        }
    }

    void showAppointmentDialog(Appointment rdvToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_rdv, null);
        builder.setView(view);

        TextView tvTitleDialog = view.findViewById(R.id.tvTitleDialog);
        EditText etTitle = view.findViewById(R.id.etDialogTitle);
        EditText etDate = view.findViewById(R.id.etDialogDate);
        EditText etTime = view.findViewById(R.id.etDialogTime);

        // --- CONTRÔLE DE SAISIE ---
        etDate.setFocusable(false);
        etTime.setFocusable(false);

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view1, year1, month1, dayOfMonth) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                        etDate.setText(formattedDate);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view12, hourOfDay, minute1) -> {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        etTime.setText(formattedTime);
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        if (rdvToEdit != null) {
            tvTitleDialog.setText("Modifier le rendez-vous");
            etTitle.setText(rdvToEdit.title);
            etDate.setText(rdvToEdit.date);
            etTime.setText(rdvToEdit.time);
        }

        String btnText = (rdvToEdit == null) ? "Ajouter" : "Modifier";
        builder.setPositiveButton(btnText, null);
        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t = etTitle.getText().toString().trim();
            String d = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (t.isEmpty()) { etTitle.setError("Requis"); return; }
            if (d.isEmpty()) { Toast.makeText(this, "Date requise", Toast.LENGTH_SHORT).show(); return; }
            if (time.isEmpty()) { Toast.makeText(this, "Heure requise", Toast.LENGTH_SHORT).show(); return; }

            // 1. Sauvegarde en Base de Données
            if (rdvToEdit == null) {
                myDB.addAppointment(t, d, time);
                Toast.makeText(this, "Rendez-vous ajouté !", Toast.LENGTH_SHORT).show();
            } else {
                myDB.updateAppointment(rdvToEdit.id, t, d, time);
                Toast.makeText(this, "Rendez-vous modifié !", Toast.LENGTH_SHORT).show();
            }

            // 2. PROGRAMMATION DE LA NOTIFICATION (NOUVEAU)
            scheduleNotification(t, d, time);

            // 3. Rafraichissement
            loadData();
            adapter.notifyDataSetChanged();
            adapter = new AppointmentAdapter(this, rdvList, appointment -> showAppointmentDialog(appointment));
            recyclerView.setAdapter(adapter);

            dialog.dismiss();
        });
    }

    // --- NOUVELLE MÉTHODE POUR PROGRAMMER L'ALARME ---
    private void scheduleNotification(String title, String dateStr, String timeStr) {
        try {
            // Conversion Date + Heure en Millisecondes
            String fullDate = dateStr + " " + timeStr; // ex: "25/12/2025 14:30"
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(fullDate);
            long timeInMillis = date.getTime();

            // Vérifier si la date n'est pas déjà passée
            if (timeInMillis < System.currentTimeMillis()) {
                return; // On ne notifie pas le passé
            }

            // Préparation de l'Intent vers le Receiver
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", title);

            // ID unique basé sur le temps pour ne pas écraser les autres alarmes
            int requestCode = (int) (timeInMillis / 1000);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Programmation de l'alarme
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // setExactAndAllowWhileIdle permet de sonner même si le téléphone est en veille
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                Toast.makeText(this, "Rappel programmé ✅", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur programmation alarme", Toast.LENGTH_SHORT).show();
        }
    }
}