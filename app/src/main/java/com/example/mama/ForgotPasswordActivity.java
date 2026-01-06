package com.example.mama;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail, etNewPass, etConfirmPass;
    Button btnCheck, btnReset;
    LinearLayout layoutPassword;
    MyDatabaseHelper myDB;
    String emailUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etResetEmail);
        etNewPass = findViewById(R.id.etNewPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        btnCheck = findViewById(R.id.btnCheckEmail);
        btnReset = findViewById(R.id.btnResetPassword);
        layoutPassword = findViewById(R.id.layoutResetPassword);

        myDB = new MyDatabaseHelper(this);

        // 1. Vérifier l'email
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailUser = etEmail.getText().toString().trim();
                if(emailUser.isEmpty()){
                    Toast.makeText(ForgotPasswordActivity.this, "Entrez un email", Toast.LENGTH_SHORT).show();
                } else {
                    boolean exists = myDB.checkEmail(emailUser);
                    if(exists){
                        // Email trouvé : On affiche les champs mot de passe
                        layoutPassword.setVisibility(View.VISIBLE);
                        etEmail.setEnabled(false); // On bloque l'email
                        btnCheck.setEnabled(false);
                        Toast.makeText(ForgotPasswordActivity.this, "Email vérifié ! Entrez le nouveau mot de passe.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Email introuvable", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 2. Mettre à jour le mot de passe
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = etNewPass.getText().toString();
                String confirm = etConfirmPass.getText().toString();

                if(pass.equals("") || confirm.equals("")){
                    Toast.makeText(ForgotPasswordActivity.this, "Remplissez tout", Toast.LENGTH_SHORT).show();
                } else {
                    if(pass.equals(confirm)){
                        // Mise à jour
                        boolean updated = myDB.updatePassword(emailUser, pass);
                        if(updated){
                            Toast.makeText(ForgotPasswordActivity.this, "Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show();
                            finish(); // Retour au login
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Erreur système", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}