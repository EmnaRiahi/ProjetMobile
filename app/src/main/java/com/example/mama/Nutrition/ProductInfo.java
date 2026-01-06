package com.example.mama.Nutrition;
import java.io.Serializable;
import java.util.List;

public class ProductInfo implements Serializable {

    private String barcode;
    private String productName;
    private String brands;
    private String quantity;
    private String imageUrl;
    private String nutriScore;
    private int novaGroup;
    private String ingredients;
    private List<String> additives;
    private List<String> allergens;
    private List<String> labels;

    // Informations nutritionnelles (pour 100g)
    private double energyKcal;
    private double fat;
    private double saturatedFat;
    private double carbohydrates;
    private double sugars;
    private double fiber;
    private double proteins;
    private double salt;

    public ProductInfo() {}

    // Getters
    public String getBarcode() {
        return barcode;
    }

    public String getProductName() {
        return productName;
    }

    public String getBrands() {
        return brands;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getNutriScore() {
        return nutriScore;
    }

    public int getNovaGroup() {
        return novaGroup;
    }

    public String getIngredients() {
        return ingredients;
    }

    public List<String> getAdditives() {
        return additives;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public List<String> getLabels() {
        return labels;
    }

    public double getEnergyKcal() {
        return energyKcal;
    }

    public double getFat() {
        return fat;
    }

    public double getSaturatedFat() {
        return saturatedFat;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public double getSugars() {
        return sugars;
    }

    public double getFiber() {
        return fiber;
    }

    public double getProteins() {
        return proteins;
    }

    public double getSalt() {
        return salt;
    }

    // Setters
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setNutriScore(String nutriScore) {
        this.nutriScore = nutriScore;
    }

    public void setNovaGroup(int novaGroup) {
        this.novaGroup = novaGroup;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setAdditives(List<String> additives) {
        this.additives = additives;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setEnergyKcal(double energyKcal) {
        this.energyKcal = energyKcal;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public void setSaturatedFat(double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public void setSugars(double sugars) {
        this.sugars = sugars;
    }

    public void setFiber(double fiber) {
        this.fiber = fiber;
    }

    public void setProteins(double proteins) {
        this.proteins = proteins;
    }

    public void setSalt(double salt) {
        this.salt = salt;
    }

    // Méthodes utilitaires
    public String getNutriScoreColor() {
        if (nutriScore == null || nutriScore.isEmpty()) {
            return "#CCCCCC";
        }

        switch (nutriScore.toUpperCase()) {
            case "A":
                return "#038141";
            case "B":
                return "#85BB2F";
            case "C":
                return "#FECB02";
            case "D":
                return "#EE8100";
            case "E":
                return "#E63E11";
            default:
                return "#CCCCCC";
        }
    }

    public String getNovaGroupDescription() {
        switch (novaGroup) {
            case 1:
                return "Aliments non transformés ou peu transformés";
            case 2:
                return "Ingrédients culinaires transformés";
            case 3:
                return "Aliments transformés";
            case 4:
                return "Produits ultra-transformés";
            default:
                return "Niveau de transformation inconnu";
        }
    }

    public boolean hasAdditives() {
        return additives != null && !additives.isEmpty();
    }

    public boolean hasAllergens() {
        return allergens != null && !allergens.isEmpty();
    }
}