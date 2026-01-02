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
        etWeek = findViewById(R.id.etWeek);         // Nouveau
        etSymptoms = findViewById(R.id.etSymptoms); // Nouveau
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

                // 1. Vérification Champs Vides
                if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || weekStr.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Vérification Format Email (Le plus important !)
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Format email invalide (ex: nom@mail.com)");
                    etEmail.requestFocus();
                    return;
                }

                // 3. Vérification Longueur Mot de passe
                if (pass.length() < 6) {
                    etPassword.setError("Le mot de passe doit faire au moins 6 caractères");
                    etPassword.requestFocus();
                    return;
                }

                // 4. Vérification Semaine de grossesse
                int week = Integer.parseInt(weekStr);
                if(week < 1 || week > 42) {
                    etWeek.setError("La semaine doit être entre 1 et 42");
                    etWeek.requestFocus();
                    return;
                }

                // Tout est bon, on inscrit !
                myDB.addUser(name, email, pass, week, symptoms);
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