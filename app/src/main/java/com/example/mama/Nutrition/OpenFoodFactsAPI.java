package com.example.mama.Nutrition;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenFoodFactsAPI {

    private static final String TAG = "OpenFoodFactsAPI";
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2/product/";

    public static ProductInfo getProductInfo(String barcode) {
        try {
            URL url = new URL(BASE_URL + barcode + ".json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "NutritionTracker/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return parseProductInfo(response.toString());
            } else {
                Log.e(TAG, "Erreur HTTP: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération des infos produit", e);
        }

        return null;
    }

    private static ProductInfo parseProductInfo(String jsonString) {
        try {
            JSONObject jsonResponse = new JSONObject(jsonString);

            // Vérifier si le produit existe
            int status = jsonResponse.getInt("status");
            if (status == 0) {
                return null;
            }

            JSONObject product = jsonResponse.getJSONObject("product");

            ProductInfo productInfo = new ProductInfo();

            // Informations de base
            productInfo.setBarcode(product.optString("code", ""));
            productInfo.setProductName(product.optString("product_name", "Produit inconnu"));
            productInfo.setBrands(product.optString("brands", ""));
            productInfo.setQuantity(product.optString("quantity", ""));
            productInfo.setImageUrl(product.optString("image_url", ""));

            // Nutri-Score
            productInfo.setNutriScore(product.optString("nutriscore_grade", "").toUpperCase());

            // Nova Group (niveau de transformation)
            productInfo.setNovaGroup(product.optInt("nova_group", 0));

            // Ingrédients
            productInfo.setIngredients(product.optString("ingredients_text", ""));

            // Additifs
            List<String> additives = new ArrayList<>();
            JSONArray additivesArray = product.optJSONArray("additives_tags");
            if (additivesArray != null) {
                for (int i = 0; i < additivesArray.length(); i++) {
                    String additive = additivesArray.getString(i)
                            .replace("en:", "")
                            .replace("-", " ")
                            .toUpperCase();
                    additives.add(additive);
                }
            }
            productInfo.setAdditives(additives);

            // Allergènes
            List<String> allergens = new ArrayList<>();
            JSONArray allergensArray = product.optJSONArray("allergens_tags");
            if (allergensArray != null) {
                for (int i = 0; i < allergensArray.length(); i++) {
                    String allergen = allergensArray.getString(i)
                            .replace("en:", "")
                            .replace("-", " ");
                    allergens.add(allergen);
                }
            }
            productInfo.setAllergens(allergens);

            // Informations nutritionnelles (pour 100g)
            JSONObject nutriments = product.optJSONObject("nutriments");
            if (nutriments != null) {
                productInfo.setEnergyKcal(nutriments.optDouble("energy-kcal_100g", 0));
                productInfo.setFat(nutriments.optDouble("fat_100g", 0));
                productInfo.setSaturatedFat(nutriments.optDouble("saturated-fat_100g", 0));
                productInfo.setCarbohydrates(nutriments.optDouble("carbohydrates_100g", 0));
                productInfo.setSugars(nutriments.optDouble("sugars_100g", 0));
                productInfo.setFiber(nutriments.optDouble("fiber_100g", 0));
                productInfo.setProteins(nutriments.optDouble("proteins_100g", 0));
                productInfo.setSalt(nutriments.optDouble("salt_100g", 0));
            }

            // Labels (bio, vegan, etc.)
            List<String> labels = new ArrayList<>();
            JSONArray labelsArray = product.optJSONArray("labels_tags");
            if (labelsArray != null) {
                for (int i = 0; i < labelsArray.length(); i++) {
                    String label = labelsArray.getString(i)
                            .replace("en:", "")
                            .replace("-", " ");
                    labels.add(label);
                }
            }
            productInfo.setLabels(labels);

            return productInfo;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du parsing JSON", e);
        }

        return null;
    }
}