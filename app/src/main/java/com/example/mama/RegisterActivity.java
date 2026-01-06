package com.example.mama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Patterns;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etWeek, etSymptoms;
    EditText etAge, etWeight, etHeight; // Nouveaux champs
    Button btnRegister;
    TextView tvGoToLogin;
    MyDatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etWeek = findViewById(R.id.etWeek);
        etSymptoms = findViewById(R.id.etSymptoms);
        
        // Initialisation nouveaux champs
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        myDB = new MyDatabaseHelper(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();
                String weekStr = etWeek.getText().toString().trim();
                String symptoms = etSymptoms.getText().toString().trim();
                String ageStr = etAge.getText().toString().trim();
                String weightStr = etWeight.getText().toString().trim();
                String heightStr = etHeight.getText().toString().trim();

                // 1. Vérification Champs Vides
                if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || weekStr.isEmpty() || 
                   ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Tous les champs (Age, Poids, Taille inclus) sont obligatoires", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Vérification Format Email
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Format email invalide");
                    etEmail.requestFocus();
                    return;
                }

                // 3. Vérification Mot de passe
                if (pass.length() < 6) {
                    etPassword.setError("Le mot de passe doit faire au moins 6 caractères");
                    etPassword.requestFocus();
                    return;
                }

                // 4. Conversions et Validations
                int week = 0, age = 0;
                double weight = 0, height = 0;
                
                try {
                    week = Integer.parseInt(weekStr);
                    age = Integer.parseInt(ageStr);
                    weight = Double.parseDouble(weightStr);
                    height = Double.parseDouble(heightStr);
                    
                    if(week < 1 || week > 42) {
                        etWeek.setError("Entre 1 et 42");
                        return;
                    }
                    if(age < 12 || age > 70) {
                        etAge.setError("Age invalide"); // Limite raisonnable pour l'appli mama
                        return;
                    }
                    if(weight < 30 || weight > 200) {
                        etWeight.setError("Poids invalide");
                        return;
                    }
                    if(height < 100 || height > 250) {
                        etHeight.setError("Taille invalide");
                        return;
                    }
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(RegisterActivity.this, "Veuillez entrer des chiffres valides", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tout est bon, on inscrit !
                myDB.addUser(name, email, pass, week, symptoms, weight, height, age);
                Toast.makeText(RegisterActivity.this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}