package com.example.mama.sport;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mama.R;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ActiviteDao activiteDao;
    private RecyclerView recyclerView;
    private List<ActiviteEntity> activityList;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        activiteDao = ActiviteDatabase.getInstance(this).activiteDao();
        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnAddSession).setOnClickListener(v ->
                new AddSessionDialog(this, null, this::loadData).show()
        );

        // Configuration du Swipe to Delete
        setupSwipeToDelete();

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            activityList = activiteDao.getAllActivities();
            runOnUiThread(() -> {
                adapter = new HistoryAdapter(activityList, new HistoryAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(ActiviteEntity activity) {
                        new AddSessionDialog(HistoryActivity.this, activity, HistoryActivity.this::loadData).show();
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
                                runOnUiThread(HistoryActivity.this::loadData);
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
                        runOnUiThread(this::loadData);
                    }).start();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}