package com.example.mama.Nutrition;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mama.R;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    private ProductInfo productInfo;
    private DatabaseHelper dbHelper;

    private ImageView ivProductImage;
    private TextView tvProductName, tvBrands, tvQuantity;
    private TextView tvNutriScore, tvNovaGroup;
    private TextView tvIngredients;
    private LinearLayout llAdditives, llAllergens, llLabels;
    private LinearLayout llNutrition;
    private Button btnAddToJournal, btnRescan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        dbHelper = new DatabaseHelper(this);

        // Récupérer les données du produit
        productInfo = (ProductInfo) getIntent().getSerializableExtra("product");

        if (productInfo == null) {
            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initViews();

        // Afficher les données
        displayProductInfo();

        // Bouton ajouter au journal
        btnAddToJournal.setOnClickListener(v -> addToJournal());

        // Bouton rescanner
        btnRescan.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, BarcodeScannerActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvBrands = findViewById(R.id.tvBrands);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvNutriScore = findViewById(R.id.tvNutriScore);
        tvNovaGroup = findViewById(R.id.tvNovaGroup);
        tvIngredients = findViewById(R.id.tvIngredients);
        llAdditives = findViewById(R.id.llAdditives);
        llAllergens = findViewById(R.id.llAllergens);
        llLabels = findViewById(R.id.llLabels);
        llNutrition = findViewById(R.id.llNutrition);
        btnAddToJournal = findViewById(R.id.btnAddToJournal);
        btnRescan = findViewById(R.id.btnRescan);
    }

    private void displayProductInfo() {
        // Nom du produit
        tvProductName.setText(productInfo.getProductName());

        // Marque
        if (productInfo.getBrands() != null && !productInfo.getBrands().isEmpty()) {
            tvBrands.setText(productInfo.getBrands());
            tvBrands.setVisibility(View.VISIBLE);
        } else {
            tvBrands.setVisibility(View.GONE);
        }

        // Quantité
        if (productInfo.getQuantity() != null && !productInfo.getQuantity().isEmpty()) {
            tvQuantity.setText(productInfo.getQuantity());
            tvQuantity.setVisibility(View.VISIBLE);
        } else {
            tvQuantity.setVisibility(View.GONE);
        }

        // Nutri-Score
        if (productInfo.getNutriScore() != null && !productInfo.getNutriScore().isEmpty()) {
            tvNutriScore.setText("Nutri-Score: " + productInfo.getNutriScore());
            tvNutriScore.setBackgroundColor(Color.parseColor(productInfo.getNutriScoreColor()));
            tvNutriScore.setVisibility(View.VISIBLE);
        } else {
            tvNutriScore.setVisibility(View.GONE);
        }

        // Nova Group
        if (productInfo.getNovaGroup() > 0) {
            tvNovaGroup.setText("NOVA " + productInfo.getNovaGroup() + ": " +
                    productInfo.getNovaGroupDescription());
            tvNovaGroup.setVisibility(View.VISIBLE);
        } else {
            tvNovaGroup.setVisibility(View.GONE);
        }

        // Ingrédients
        if (productInfo.getIngredients() != null && !productInfo.getIngredients().isEmpty()) {
            tvIngredients.setText(productInfo.getIngredients());
            tvIngredients.setVisibility(View.VISIBLE);
        } else {
            tvIngredients.setVisibility(View.GONE);
        }

        // Additifs
        displayAdditives();

        // Allergènes
        displayAllergens();

        // Labels
        displayLabels();

        // Informations nutritionnelles
        displayNutritionInfo();

        // Charger l'image du produit
        loadProductImage();
    }

    private void displayAdditives() {
        llAdditives.removeAllViews();

        if (productInfo.hasAdditives()) {
            for (String additive : productInfo.getAdditives()) {
                TextView tv = new TextView(this);
                tv.setText("• " + additive);
                tv.setTextSize(14);
                tv.setTextColor(Color.parseColor("#E63E11"));
                tv.setPadding(0, 4, 0, 4);
                llAdditives.addView(tv);
            }
            llAdditives.setVisibility(View.VISIBLE);
        } else {
            llAdditives.setVisibility(View.GONE);
        }
    }

    private void displayAllergens() {
        llAllergens.removeAllViews();

        if (productInfo.hasAllergens()) {
            for (String allergen : productInfo.getAllergens()) {
                TextView tv = new TextView(this);
                tv.setText("• " + allergen);
                tv.setTextSize(14);
                tv.setTextColor(Color.parseColor("#FF6B6B"));
                tv.setPadding(0, 4, 0, 4);
                llAllergens.addView(tv);
            }
            llAllergens.setVisibility(View.VISIBLE);
        } else {
            llAllergens.setVisibility(View.GONE);
        }
    }

    private void displayLabels() {
        llLabels.removeAllViews();

        if (productInfo.getLabels() != null && !productInfo.getLabels().isEmpty()) {
            for (String label : productInfo.getLabels()) {
                TextView tv = new TextView(this);
                tv.setText("✓ " + label);
                tv.setTextSize(14);
                tv.setTextColor(Color.parseColor("#038141"));
                tv.setPadding(0, 4, 0, 4);
                llLabels.addView(tv);
            }
            llLabels.setVisibility(View.VISIBLE);
        } else {
            llLabels.setVisibility(View.GONE);
        }
    }

    private void displayNutritionInfo() {
        llNutrition.removeAllViews();

        String[] nutritionData = {
                "Énergie: " + productInfo.getEnergyKcal() + " kcal",
                "Matières grasses: " + productInfo.getFat() + " g",
                "  dont saturées: " + productInfo.getSaturatedFat() + " g",
                "Glucides: " + productInfo.getCarbohydrates() + " g",
                "  dont sucres: " + productInfo.getSugars() + " g",
                "Fibres: " + productInfo.getFiber() + " g",
                "Protéines: " + productInfo.getProteins() + " g",
                "Sel: " + productInfo.getSalt() + " g"
        };

        for (String data : nutritionData) {
            if (!data.contains(": 0.0")) { // Ne pas afficher les valeurs à 0
                TextView tv = new TextView(this);
                tv.setText(data);
                tv.setTextSize(14);
                tv.setPadding(0, 4, 0, 4);
                llNutrition.addView(tv);
            }
        }
    }

    private void loadProductImage() {
        if (productInfo.getImageUrl() != null && !productInfo.getImageUrl().isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL(productInfo.getImageUrl());
                    InputStream input = url.openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);

                    runOnUiThread(() -> {
                        ivProductImage.setImageBitmap(bitmap);
                        ivProductImage.setVisibility(View.VISIBLE);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void addToJournal() {
        // Créer un repas avec les infos du produit
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);

        String mealName = productInfo.getProductName();
        if (productInfo.getBrands() != null && !productInfo.getBrands().isEmpty()) {
            mealName += " (" + productInfo.getBrands() + ")";
        }

        String notes = "Scanné - Code: " + productInfo.getBarcode();
        if (productInfo.getNutriScore() != null && !productInfo.getNutriScore().isEmpty()) {
            notes += "\nNutri-Score: " + productInfo.getNutriScore();
        }
        if (productInfo.getEnergyKcal() > 0) {
            notes += "\nÉnergie: " + productInfo.getEnergyKcal() + " kcal/100g";
        }

        Meal meal = new Meal(
                mealName,
                dateFormat.format(calendar.getTime()),
                timeFormat.format(calendar.getTime()),
                notes
        );

        long id = dbHelper.addMeal(meal);

        if (id > 0) {
            Toast.makeText(this, "Produit ajouté au journal", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProductDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
        }
    }
}