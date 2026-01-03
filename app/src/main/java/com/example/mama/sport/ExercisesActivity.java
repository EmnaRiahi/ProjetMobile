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
            txtTitle.setText("Indoor Exercises");
            // Pregnancy Safe Indoor
            exercises.add(new Exercise("Prenatal Yoga", "Gentle stretching and breathing exercises.", android.R.drawable.ic_menu_mylocation));
            exercises.add(new Exercise("Wall Push-ups", "Strengthens chest and arms without strain.", android.R.drawable.ic_menu_rotate));
            exercises.add(new Exercise("Pelvic Tilts", "Strengthens abdominal muscles and relieves back pain.", android.R.drawable.ic_menu_sort_by_size));
            exercises.add(new Exercise("Squats (Supported)", "Use a chair for stability. Great for legs.", android.R.drawable.ic_menu_week));
        } else {
            txtTitle.setText("Outdoor Exercises");
            // Pregnancy Safe Outdoor
            exercises.add(new Exercise("Walking", "The best low-impact cardiovascular exercise.", android.R.drawable.ic_menu_directions));
            exercises.add(new Exercise("Swimming", "Relieves joint pressure and works all muscles.", android.R.drawable.ic_menu_gallery));
            exercises.add(new Exercise("Stationary Cycling", "Low risk of falling, good for cardio.", android.R.drawable.ic_menu_compass));
        }

        recyclerView.setAdapter(new ExerciseAdapter(exercises));
    }
}
