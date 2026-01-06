package com.example.mama.Nutrition;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mama.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditMealActivity extends AppCompatActivity {

    private EditText etMealName, etDate, etTime, etNotes;
    private Button btnUpdate, btnCancel;
    private DatabaseHelper dbHelper;
    private Calendar calendar;
    private int mealId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meal);

        dbHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();

        mealId = getIntent().getIntExtra("meal_id", -1);

        if (mealId == -1) {
            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        etMealName = findViewById(R.id.etMealName);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        // Charger les données du repas
        loadMealData();

        // DatePicker
        etDate.setOnClickListener(v -> showDatePicker());

        // TimePicker
        etTime.setOnClickListener(v -> showTimePicker());

        // Bouton Mettre à jour
        btnUpdate.setOnClickListener(v -> updateMeal());

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadMealData() {
        Meal meal = dbHelper.getMeal(mealId);
        if (meal != null) {
            etMealName.setText(meal.getName());
            etDate.setText(meal.getDate());
            etTime.setText(meal.getTime());
            etNotes.setText(meal.getNotes());

            // Mettre à jour le calendar avec la date/heure du repas
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
                calendar.setTime(sdf.parse(meal.getDate() + " " + meal.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
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

    private void updateMeal() {
        String name = etMealName.getText().toString().trim();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String notes = etNotes.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer le nom du repas", Toast.LENGTH_SHORT).show();
            return;
        }

        Meal meal = new Meal(mealId, name, date, time, notes);
        int result = dbHelper.updateMeal(meal);

        if (result > 0) {
            Toast.makeText(this, "Repas mis à jour avec succès", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }
}