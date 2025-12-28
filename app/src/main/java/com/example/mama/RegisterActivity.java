package com.example.mama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

                if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || weekStr.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show();
                } else {
                    int week = Integer.parseInt(weekStr);

                    // Vérification simple de la semaine
                    if(week < 1 || week > 42) {
                        Toast.makeText(RegisterActivity.this, "Semaine invalide (1-42)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Ajout dans la base avec les nouvelles infos
                    myDB.addUser(name, email, pass, week, symptoms);

                    Toast.makeText(RegisterActivity.this, "Compte créé ! Bienvenue.", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}