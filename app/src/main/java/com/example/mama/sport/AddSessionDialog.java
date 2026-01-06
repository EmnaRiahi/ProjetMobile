package com.example.mama.sport;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.mama.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddSessionDialog extends Dialog {
    private EditText edtStepGoal;
    private RadioGroup radioType;
    private Button btnSave;
    private Runnable onSaved;
    private ActiviteEntity existingActivity;
    private int defaultSteps;

    public AddSessionDialog(@NonNull Context context, ActiviteEntity activity, int defaultSteps, Runnable onSaved) {
        super(context);
        this.existingActivity = activity;
        this.defaultSteps = defaultSteps;
        this.onSaved = onSaved;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_session);

        // Initialize Views
        edtStepGoal = findViewById(R.id.edtStepGoal);
        radioType = findViewById(R.id.radioType);
        btnSave = findViewById(R.id.btnSave);

        // Load current goal from SharedPreferences OR use Passed Default
        SharedPreferences prefs = getContext().getSharedPreferences("SportPrefs", Context.MODE_PRIVATE);
        
        // Priority: 1. Existing Activity 2. Prefs (User manual override) 3. Personalized Plan (Default)
        // If user manually changed it before, we might want to respect that? 
        // User request says "default values should be the ones suggested".
        // So let's prefer the passed defaultSteps if no prefs exist, or maybe override prefs?
        // Let's use the passed defaultSteps if it's > 0.
        
        int initialGoal = defaultSteps > 0 ? defaultSteps : prefs.getInt("step_goal", 6000);
        
        edtStepGoal.setText(String.valueOf(initialGoal));

        if (existingActivity != null) {
            edtStepGoal.setText(String.valueOf(existingActivity.targetSteps));
            if ("INDOOR".equals(existingActivity.type)) {
                radioType.check(R.id.radioIndoor);
            } else {
                radioType.check(R.id.radioOutdoor);
            }
        }

        // Save Logic
        btnSave.setOnClickListener(v -> {
            String goalStr = edtStepGoal.getText().toString();

            if (goalStr.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez saisir un objectif de pas", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int target = Integer.parseInt(goalStr);
                if (target <= 0) {
                     Toast.makeText(getContext(), "L'objectif doit être supérieur à 0", Toast.LENGTH_SHORT).show();
                     return;
                }

                new Thread(() -> {
                    ActiviteDao dao = ActiviteDatabase.getInstance(getContext()).activiteDao();
                    ActiviteEntity a = (existingActivity == null) ? new ActiviteEntity() : existingActivity;

                    // Auto-set Date/Time for new sessions
                    if (existingActivity == null) {
                        a.date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                        a.time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        a.steps = 0; // Reset steps for new session as requested
                        a.isAchieved = false;
                        a.duration = 0;
                        a.distance = 0.0;
                        a.calories = 0.0;
                    }

                    a.targetSteps = target;
                    a.type = radioType.getCheckedRadioButtonId() == R.id.radioIndoor ? "INDOOR" : "OUTDOOR";
                    
                    // Save preference for next time
                    prefs.edit().putInt("step_goal", target).apply();

                    if (existingActivity == null) {
                        dao.insert(a);
                    } else {
                        dao.update(a);
                    }

                    btnSave.post(() -> {
                        if (onSaved != null) onSaved.run();
                        dismiss();
                    });
                }).start();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Nombre invalide", Toast.LENGTH_SHORT).show();
            }
        });
    }
}