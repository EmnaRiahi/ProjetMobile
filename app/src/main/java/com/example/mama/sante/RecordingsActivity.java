package com.example.mama.sante;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecordingsActivity extends AppCompatActivity {

    private LightSensorManager lightSensorManager;
    private View rootView;

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ImageButton btnBack;
    private RecordingAdapter adapter;
    private List<File> recordingFiles;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);

        rootView = findViewById(android.R.id.content);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize light sensor
        lightSensorManager = new LightSensorManager(this);
        lightSensorManager.setThemeChangeListener(this::applyTheme);

        loadRecordings();
    }

    private void loadRecordings() {
        File dir = new File(getExternalFilesDir(null), "SleepRecordings");
        recordingFiles = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".mp3"));
            if (files != null) {
                recordingFiles.addAll(Arrays.asList(files));
                // Sort by date descending
                Collections.sort(recordingFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            }
        }

        if (recordingFiles.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new RecordingAdapter(recordingFiles, new RecordingAdapter.OnItemClickListener() {
                @Override
                public void onPlayClick(File file) {
                    playRecording(file);
                }

                @Override
                public void onDeleteClick(File file) {
                    if (file.delete()) {
                        Toast.makeText(RecordingsActivity.this, "Enregistrement supprimé", Toast.LENGTH_SHORT).show();
                        loadRecordings(); // Refresh list
                    } else {
                        Toast.makeText(RecordingsActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onDownloadClick(File file) {
                    saveToDownloads(file);
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void saveToDownloads(File file) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // API 29+ - Use MediaStore
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOWNLOADS + "/MAMA_Sleep");

            android.content.ContentResolver resolver = getContentResolver();
            android.net.Uri uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (java.io.OutputStream out = resolver.openOutputStream(uri);
                        java.io.InputStream in = new java.io.FileInputStream(file)) {

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    Toast.makeText(this, "Téléchargé dans MAMA_Sleep", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erreur de téléchargement", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // API 24-28 - Copy to Downloads directory directly
            File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS);
            File destDir = new File(downloadsDir, "MAMA_Sleep");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File destFile = new File(destDir, file.getName());
            try (java.io.OutputStream out = new java.io.FileOutputStream(destFile);
                    java.io.InputStream in = new java.io.FileInputStream(file)) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                Toast.makeText(this, "Téléchargé dans MAMA_Sleep", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur de téléchargement", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playRecording(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Lecture : " + file.getName(), Toast.LENGTH_SHORT).show();

            mediaPlayer.setOnCompletionListener(mp -> {
                Toast.makeText(this, "Lecture terminée", Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur de lecture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        lightSensorManager.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lightSensorManager.unregister();
    }

    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            rootView.setBackgroundColor(ContextCompat.getColor(this, R.color.sante_dark_background));
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.sante_dark_text_secondary));
        } else {
            rootView.setBackgroundColor(ContextCompat.getColor(this, R.color.mama_background));
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.grey_text));
        }
    }
}
