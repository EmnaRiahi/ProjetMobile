package com.example.mama.sport;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mama.R;
import android.widget.TextView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ActiviteDao activiteDao;
    private RecyclerView recyclerView;
    private List<ActiviteEntity> activityList;
    private HistoryAdapter adapter;

    private TextView txtTotalCount, txtTotalSteps;
    private com.google.android.material.button.MaterialButton btnFilterAll, btnFilterAchieved, btnFilterCurrent;
    private String currentFilter = "ALL"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        activiteDao = ActiviteDatabase.getInstance(this).activiteDao();
        
        // Init Views
        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtTotalSteps = findViewById(R.id.txtTotalSteps);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterAchieved = findViewById(R.id.btnFilterAchieved);
        btnFilterCurrent = findViewById(R.id.btnFilterCurrent);

        // Check intent default
        if (getIntent().hasExtra("filter")) {
            currentFilter = getIntent().getStringExtra("filter");
            // Map legacy UNACHIEVED to CURRENT if passed from Dashboard click
            if ("UNACHIEVED".equals(currentFilter)) currentFilter = "CURRENT"; 
        }
        updateFilterButtons();

        findViewById(R.id.btnAddSession).setOnClickListener(v ->
                new AddSessionDialog(this, null, 0, this::refreshAll).show()
        );

        // Filter Listeners
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterButtons();
            loadData();
        });

        btnFilterAchieved.setOnClickListener(v -> {
            currentFilter = "ACHIEVED";
            updateFilterButtons();
            loadData();
        });

        btnFilterCurrent.setOnClickListener(v -> {
            currentFilter = "CURRENT";
            updateFilterButtons();
            loadData();
        });

        setupSwipeToDelete();
        refreshAll();
    }

    private void updateFilterButtons() {
        int activeColor = 0xFF00B894;
        int activeText = 0xFFFFFFFF;
        
        int inactiveColor = 0x00000000;
        int inactiveText = 0xFF2D3436;
        int strokeColor = 0xFFB2BEC3;

        // Reset all
        btnFilterAll.setBackgroundColor(inactiveColor);
        btnFilterAll.setTextColor(inactiveText);
        btnFilterAll.setStrokeWidth(2);
        btnFilterAll.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeColor));

        btnFilterAchieved.setBackgroundColor(inactiveColor);
        btnFilterAchieved.setTextColor(inactiveText);
        btnFilterAchieved.setStrokeWidth(2);
        btnFilterAchieved.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeColor));

        btnFilterCurrent.setBackgroundColor(inactiveColor);
        btnFilterCurrent.setTextColor(inactiveText);
        btnFilterCurrent.setStrokeWidth(2);
        btnFilterCurrent.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeColor));

        // Highlight selected
        if ("ALL".equals(currentFilter)) {
            btnFilterAll.setBackgroundColor(0xFF2D3436); // Logic from previous: simple dark for ALL
            btnFilterAll.setTextColor(activeText);
            btnFilterAll.setStrokeWidth(0);
        } else if ("ACHIEVED".equals(currentFilter)) {
            btnFilterAchieved.setBackgroundColor(activeColor);
            btnFilterAchieved.setTextColor(activeText);
            btnFilterAchieved.setStrokeWidth(0);
        } else if ("CURRENT".equals(currentFilter)) {
            btnFilterCurrent.setBackgroundColor(0xFFFF9F43); // Orange for 'Current' maybe? Or keep standard
            btnFilterCurrent.setTextColor(activeText);
            btnFilterCurrent.setStrokeWidth(0);
        }
    }

    private void refreshAll() {
        loadStats();
        loadData();
    }

    private void loadStats() {
        new Thread(() -> {
            int count = activiteDao.getCount();
            int totalSteps = activiteDao.getTotalSteps();
            runOnUiThread(() -> {
                txtTotalCount.setText(String.valueOf(count));
                txtTotalSteps.setText(totalSteps > 1000 ? String.format(java.util.Locale.US, "%.1fk", totalSteps / 1000.0) : String.valueOf(totalSteps));
            });
        }).start(); 
    }

    private void loadData() {
        new Thread(() -> {
            if ("ACHIEVED".equals(currentFilter)) {
                activityList = activiteDao.getAchievedSessions();
            } else if ("CURRENT".equals(currentFilter)) {
                activityList = activiteDao.getUnachievedSessions();
            } else {
                activityList = activiteDao.getAllActivities();
            }
            
            runOnUiThread(() -> {
                adapter = new HistoryAdapter(activityList, new HistoryAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(ActiviteEntity activity) {
                        new AddSessionDialog(HistoryActivity.this, activity, 0, HistoryActivity.this::refreshAll).show();
                    }

                    @Override
                    public void onItemLongClick(ActiviteEntity activity) {
                        // Optionnel : garder aussi le clic long
                        showDeleteDialog(activity);
                    }
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false; // On ne gère pas le déplacement haut/bas
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Récupérer la position de l'élément balayé
                int position = viewHolder.getAdapterPosition();
                ActiviteEntity activityToDelete = activityList.get(position);

                // Confirmer la suppression
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("Supprimer ?")
                        .setMessage("Voulez-vous supprimer cette session ?")
                        .setPositiveButton("Oui", (d, w) -> {
                            new Thread(() -> {
                                activiteDao.delete(activityToDelete);
                        runOnUiThread(HistoryActivity.this::refreshAll);
                            }).start();
                        })
                        .setNegativeButton("Annuler", (d, w) -> {
                            // Si annulé, on remet l'item à sa place
                            adapter.notifyItemChanged(position);
                        })
                        .setCancelable(false)
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showDeleteDialog(ActiviteEntity activity) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer ?")
                .setMessage("Voulez-vous supprimer cette session ?")
                .setPositiveButton("Oui", (d, w) -> {
                    new Thread(() -> {
                        activiteDao.delete(activity);
                        runOnUiThread(this::refreshAll);
                    }).start();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}