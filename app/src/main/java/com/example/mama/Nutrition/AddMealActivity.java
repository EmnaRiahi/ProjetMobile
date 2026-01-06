package com.example.mama.Nutrition;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mama.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {

    private EditText etMealName, etDate, etTime, etNotes;
    private Button btnSave, btnCancel;
    private DatabaseHelper dbHelper;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        dbHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();

        // Initialiser les vues
        etMealName = findViewById(R.id.etMealName);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Définir la date et l'heure actuelles par défaut
        updateDateField();
        updateTimeField();

        // Animer l'entrée des champs
        animateFormFields();

        // DatePicker
        etDate.setOnClickListener(v -> {
            animateFieldClick(v);
            showDatePicker();
        });

        // TimePicker
        etTime.setOnClickListener(v -> {
            animateFieldClick(v);
            showTimePicker();
        });

        // Bouton Enregistrer
        btnSave.setOnClickListener(v -> {
            animateButton(v);
            v.postDelayed(this::saveMeal, 150);
        });

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> {
            animateButton(v);
            v.postDelayed(this::finish, 150);
        });
    }

    private void animateFormFields() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        etMealName.startAnimation(slideUp);
        etDate.postDelayed(() -> etDate.startAnimation(slideUp), 50);
        etTime.postDelayed(() -> etTime.startAnimation(slideUp), 100);
        etNotes.postDelayed(() -> etNotes.startAnimation(slideUp), 150);
        btnSave.postDelayed(() -> btnSave.startAnimation(slideUp), 200);
        btnCancel.postDelayed(() -> btnCancel.startAnimation(slideUp), 250);
    }

    private void animateFieldClick(android.view.View view) {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        view.startAnimation(pulse);
    }

    private void animateButton(android.view.View button) {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        button.startAnimation(pulse);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();

                    // Animation après sélection
                    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                    etDate.startAnimation(fadeIn);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeField();

                    // Animation après sélection
                    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                    etTime.startAnimation(fadeIn);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeField() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        etTime.setText(sdf.format(calendar.getTime()));
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String notes = etNotes.getText().toString().trim();

        if (name.isEmpty()) {
            // Animation shake pour indiquer l'erreur
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            etMealName.startAnimation(shake);
            showAnimatedToast("⚠️ Veuillez entrer le nom du repas");
            return;
        }

        Meal meal = new Meal(name, date, time, notes);
        long id = dbHelper.addMeal(meal);

        if (id > 0) {
            showAnimatedToast("✨ Repas enregistré avec succès");
            finish();
        } else {
            showAnimatedToast("❌ Erreur lors de l'enregistrement");
        }
    }

    private void showAnimatedToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
    }
}