
        package com.example.mama.sport;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import com.example.mama.R;
import com.example.mama.sport.ActiviteDao;
import com.example.mama.sport.ActiviteDatabase;
import com.example.mama.sport.ActiviteEntity;

import java.util.Calendar;
import java.util.Locale;

public class AddSessionDialog extends Dialog {
    private EditText edtDuration, edtSteps, edtStepGoal;
    private RadioGroup radioType;
    private Button btnSave, btnPickDate, btnPickTime;
    private Runnable onSaved;
    private ActiviteEntity existingActivity;

    // Variables pour stocker les choix temporairement
    private String selectedDate = "";
    private String selectedTime = "";

    public AddSessionDialog(@NonNull Context context, ActiviteEntity activity, Runnable onSaved) {
        super(context);
        this.existingActivity = activity;
        this.onSaved = onSaved;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_session);

        edtDuration = findViewById(R.id.edtDuration);
        edtSteps = findViewById(R.id.edtSteps);
        edtStepGoal = findViewById(R.id.edtStepGoal);
        radioType = findViewById(R.id.radioType);
        btnSave = findViewById(R.id.btnSave);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);

        // Pré-remplir avec l'objectif actuel
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("SportPrefs", Context.MODE_PRIVATE);
        edtStepGoal.setText(String.valueOf(prefs.getInt("step_goal", 6000)));

        // Initialisation si modification
        if (existingActivity != null) {
            edtDuration.setText(String.valueOf(existingActivity.duration));
            edtSteps.setText(String.valueOf(existingActivity.steps));
            selectedDate = existingActivity.date;
            selectedTime = existingActivity.time;
            btnPickDate.setText(selectedDate);
            btnPickTime.setText(selectedTime);
            if ("INDOOR".equals(existingActivity.type)) radioType.check(R.id.radioIndoor);
            else radioType.check(R.id.radioOutdoor);
        }

        // --- GESTION DU CALENDRIER ---
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                btnPickDate.setText(selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // --- GESTION DE L'HORLOGE ---
        btnPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                btnPickTime.setText(selectedTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        // --- SAUVEGARDE ---
        btnSave.setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                // Optionnel: Ajouter un Toast pour dire de remplir la date/heure
                return;
            }

            new Thread(() -> {
                ActiviteDao dao = ActiviteDatabase.getInstance(getContext()).activiteDao();
                ActiviteEntity a = (existingActivity == null) ? new ActiviteEntity() : existingActivity;

                a.duration = Integer.parseInt(edtDuration.getText().toString());
                a.steps = Integer.parseInt(edtSteps.getText().toString());
                a.type = radioType.getCheckedRadioButtonId() == R.id.radioIndoor ? "INDOOR" : "OUTDOOR";
                a.date = selectedDate;
                a.time = selectedTime;

                // --- AJOUT : Sauvegarder le nouvel objectif ---
                String goalStr = edtStepGoal.getText().toString();
                if (!goalStr.isEmpty()) {
                    int newGoal = Integer.parseInt(goalStr);
                    prefs.edit().putInt("step_goal", newGoal).apply();
                }

                if (existingActivity == null) dao.insert(a);
                else dao.update(a);

                btnSave.post(() -> {
                    if (onSaved != null) onSaved.run();
                    dismiss();
                });
            }).start();
        });
    }
}