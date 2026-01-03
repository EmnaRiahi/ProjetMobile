package com.example.mama.Nutrition;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mama.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeScanner";
    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Vérifier la permission caméra
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        // Bouton retour
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bouton saisie manuelle
        findViewById(R.id.btnManualEntry).setOnClickListener(v -> {
            Intent intent = new Intent(BarcodeScannerActivity.this, AddMealActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permission caméra requise", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erreur lors du démarrage de la caméra", e);
                Toast.makeText(this, "Erreur caméra", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image Analysis pour le scan
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new BarcodeAnalyzer());

        // Sélection caméra arrière
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
            );
        } catch (Exception e) {
            Log.e(TAG, "Erreur de liaison caméra", e);
        }
    }

    private class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        private final MultiFormatReader reader = new MultiFormatReader();

        @Override
        public void analyze(@NonNull ImageProxy image) {
            if (!isScanning) {
                image.close();
                return;
            }

            try {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                        data,
                        image.getWidth(),
                        image.getHeight(),
                        0, 0,
                        image.getWidth(),
                        image.getHeight(),
                        false
                );

                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = reader.decode(bitmap);

                // Code-barres détecté
                String barcode = result.getText();
                isScanning = false;

                runOnUiThread(() -> {
                    Toast.makeText(BarcodeScannerActivity.this,
                            "Code scanné: " + barcode, Toast.LENGTH_SHORT).show();
                    fetchProductInfo(barcode);
                });

            } catch (NotFoundException e) {
                // Pas de code-barres trouvé dans cette frame
            } finally {
                image.close();
            }
        }
    }

    private void fetchProductInfo(String barcode) {
        // Afficher un indicateur de chargement
        runOnUiThread(() -> findViewById(R.id.progressBar).setVisibility(View.VISIBLE));

        // Lancer la requête API dans un thread séparé
        new Thread(() -> {
            ProductInfo productInfo = OpenFoodFactsAPI.getProductInfo(barcode);

            runOnUiThread(() -> {
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (productInfo != null) {
                    // Ouvrir l'écran de détails du produit
                    Intent intent = new Intent(BarcodeScannerActivity.this,
                            ProductDetailsActivity.class);
                    intent.putExtra("product", productInfo);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(BarcodeScannerActivity.this,
                            "Produit non trouvé", Toast.LENGTH_LONG).show();
                    isScanning = true; // Reprendre le scan
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}