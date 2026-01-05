package com.example.mama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    TextView tvForgotPassword;
    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvGoToRegister;
    MyDatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        myDB = new MyDatabaseHelper(this);

        // Clic sur Connexion
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if(email.isEmpty() || pass.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {
                    // Vérification avec SQLite
                    boolean isLogged = myDB.checkUser(email, pass);

                    if(isLogged){
                        Toast.makeText(LoginActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();

                        // Save session
                        getSharedPreferences("user_session", MODE_PRIVATE)
                                .edit()
                                .putString("email", email)
                                .apply();

                        // On redirige vers le nouveau Tableau de Bord (HomeDashboardActivity)
                        Intent intent = new Intent(LoginActivity.this, HomeDashboardActivity.class);
                        startActivity(intent);
                        finish(); // Ferme Login pour ne pas revenir en arrière
                    } else {
                        Toast.makeText(LoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Aller vers Inscription
        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Mot de passe oublié
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }
}