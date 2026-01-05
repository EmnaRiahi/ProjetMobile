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
            exercises.add(new Exercise("Prenatal Yoga", "Gentle stretching and breathing exercises.", android.R.drawable.ic_menu_mylocation, "https://www.youtube.com/watch?v=B87FpWtkIKA"));
            exercises.add(new Exercise("Wall Push-ups", "Strengthens chest and arms without strain.", android.R.drawable.ic_menu_rotate, "https://www.youtube.com/watch?v=q-JZSn4Z2X0"));
            exercises.add(new Exercise("Pelvic Tilts", "Relieves back pain and strengthens core.", android.R.drawable.ic_menu_sort_by_size, "https://www.youtube.com/watch?v=moa4h-rjuNE"));
            exercises.add(new Exercise("Squats", "Great for leg strength and mobility.", android.R.drawable.ic_menu_week, "https://www.youtube.com/watch?v=e8_pNWIBa2M"));
        } else {
            txtTitle.setText("Outdoor Exercises");
            exercises.add(new Exercise("Walking / Running", "Low-impact cardio for overall health.", android.R.drawable.ic_menu_directions, "https://www.youtube.com/watch?v=oQBGdPbGYVQ"));
            exercises.add(new Exercise("Swimming", "Zero-gravity exercise for full body relief.", android.R.drawable.ic_menu_gallery, "https://www.youtube.com/watch?v=wIDjz9uqN4w"));
            exercises.add(new Exercise("Cycling", "Safe, steady-paced outdoor activity.", android.R.drawable.ic_menu_compass, "https://www.youtube.com/watch?v=SFlVRWt5DqQ"));
        }

        recyclerView.setAdapter(new ExerciseAdapter(exercises));
    }
}
