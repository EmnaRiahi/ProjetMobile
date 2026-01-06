package com.example.mama;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivProfilePicture;
    private ImageButton btnEditImage;
    private TextInputEditText etName, etAge, etWeight, etHeight, etWeek, etSymptoms;
    private Button btnSave;
    private MyDatabaseHelper myDB;
    private String userEmail;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnEditImage = findViewById(R.id.btnEditImage);
        etName = findViewById(R.id.etSettingsName);
        etAge = findViewById(R.id.etSettingsAge);
        etWeight = findViewById(R.id.etSettingsWeight);
        etHeight = findViewById(R.id.etSettingsHeight);
        etWeek = findViewById(R.id.etSettingsWeek);
        etSymptoms = findViewById(R.id.etSettingsSymptoms);
        btnSave = findViewById(R.id.btnSaveSettings);

        myDB = new MyDatabaseHelper(this);

        // Get user session
        userEmail = getSharedPreferences("user_session", MODE_PRIVATE).getString("email", "");

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Session expirée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserData();

        btnEditImage.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        Cursor cursor = myDB.getUserByEmail(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("fullname")));
            etAge.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("age"))));
            etWeight.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))));
            etHeight.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("height"))));
            etWeek.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("pregnancy_week"))));
            etSymptoms.setText(cursor.getString(cursor.getColumnIndexOrThrow("symptoms")));

            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image"));
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    ivProfilePicture.setImageURI(Uri.parse(imagePath));
                    imageUri = Uri.parse(imagePath);
                } catch (Exception e) {
                    ivProfilePicture.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
            cursor.close();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            
            // On copie l'image en local tout de suite ou après ? 
            // On va le faire au moment de la sauvegarde pour ne pas encombrer si l'utilisateur annule.
            // Mais on l'affiche tout de suite.
            imageUri = selectedImageUri;
            try {
                ivProfilePicture.setImageURI(imageUri);
            } catch (Exception e) {
                Toast.makeText(this, "Erreur d'affichage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "profile_picture.jpg");
            OutputStream outputStream = new FileOutputStream(file);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveUserData() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weekStr = etWeek.getText().toString().trim();
        String symptoms = etSymptoms.getText().toString().trim();
        String imagePath = (imageUri != null) ? imageUri.toString() : "";

        if (name.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || weekStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            int week = Integer.parseInt(weekStr);

            // Si une nouvelle image a été choisie (et que c'est une URI de type content://)
            if (imageUri != null && imageUri.toString().startsWith("content")) {
                String internalPath = saveImageToInternalStorage(imageUri);
                if (internalPath != null) {
                    imagePath = internalPath;
                }
            }

            myDB.updateUser(userEmail, name, age, weight, height, week, symptoms, imagePath);
            Toast.makeText(this, "Profil mis à jour !", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez entrer des chiffres valides", Toast.LENGTH_SHORT).show();
        }
    }
}
