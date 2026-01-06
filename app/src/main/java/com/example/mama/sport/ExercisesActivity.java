package com.example.mama.sport;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mama.R;
import java.util.ArrayList;
import java.util.List;

public class ExercisesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        txtTitle = findViewById(R.id.txtTitle);
        recyclerView = findViewById(R.id.recyclerExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String type = getIntent().getStringExtra("type");
        if (type == null) type = "INDOOR";

        List<Exercise> exercises = new ArrayList<>();

        if ("INDOOR".equals(type)) {
            txtTitle.setText("Exercices en intérieur");
            exercises.add(new Exercise("Yoga prénatal", "Étirements doux et exercices de respiration.", android.R.drawable.ic_menu_mylocation, "https://www.youtube.com/watch?v=B87FpWtkIKA"));
            exercises.add(new Exercise("Pompes au mur", "Renforce la poitrine et les bras sans effort excessif.", android.R.drawable.ic_menu_rotate, "https://www.youtube.com/watch?v=q-JZSn4Z2X0"));
            exercises.add(new Exercise("Basculements du bassin", "Soulage les maux de dos et renforce la sangle abdominale.", android.R.drawable.ic_menu_sort_by_size, "https://www.youtube.com/watch?v=moa4h-rjuNE"));
            exercises.add(new Exercise("Squats", "Excellent pour la force et la mobilité des jambes.", android.R.drawable.ic_menu_week, "https://www.youtube.com/watch?v=e8_pNWIBa2M"));
        } else {
            txtTitle.setText("Exercices en extérieur");
            exercises.add(new Exercise("Marche / Course", "Cardio à faible impact pour une santé globale.", android.R.drawable.ic_menu_directions, "https://www.youtube.com/watch?v=oQBGdPbGYVQ"));
            exercises.add(new Exercise("Natation", "Exercice en apesanteur pour un soulagement de tout le corps.", android.R.drawable.ic_menu_gallery, "https://www.youtube.com/watch?v=wIDjz9uqN4w"));
            exercises.add(new Exercise("Cyclisme", "Activité de plein air sécurisée et à rythme régulier.", android.R.drawable.ic_menu_compass, "https://www.youtube.com/watch?v=SFlVRWt5DqQ"));
        }

        recyclerView.setAdapter(new ExerciseAdapter(exercises));
    }
}
