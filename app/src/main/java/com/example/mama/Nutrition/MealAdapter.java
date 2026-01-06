package com.example.mama.Nutrition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.R;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;
    private OnMealClickListener listener;
    private int lastPosition = -1;

    public interface OnMealClickListener {
        void onEditClick(Meal meal);
        void onDeleteClick(Meal meal);
    }

    public MealAdapter(List<Meal> mealList, OnMealClickListener listener) {
        this.mealList = mealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);

        holder.tvMealName.setText(meal.getName());
        holder.tvDateTime.setText(meal.getDate() + " à " + meal.getTime());

        if (meal.getNotes() != null && !meal.getNotes().isEmpty()) {
            holder.tvNotes.setText(meal.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Animation d'entrée pour les nouvelles cartes
        setAnimation(holder.itemView, position);

        // Animation pulse sur les boutons
        holder.btnEdit.setOnClickListener(v -> {
            animateButton(v);
            if (listener != null) {
                // Délai pour laisser l'animation se jouer
                v.postDelayed(() -> listener.onEditClick(meal), 150);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            animateButton(v);
            if (listener != null) {
                // Animation de suppression
                animateItemRemoval(holder.itemView, () -> {
                    listener.onDeleteClick(meal);
                });
            }
        });

        // Animation au clic sur la carte entière
        holder.itemView.setOnClickListener(v -> {
            Animation pulse = AnimationUtils.loadAnimation(v.getContext(), R.anim.pulse);
            v.startAnimation(pulse);
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    // Animation d'entrée pour chaque item
    private void setAnimation(View viewToAnimate, int position) {
        // Animer seulement les nouveaux items
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(
                    viewToAnimate.getContext(),
                    R.anim.slide_up
            );
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    // Animation pour les boutons
    private void animateButton(View button) {
        Animation pulse = AnimationUtils.loadAnimation(button.getContext(), R.anim.pulse);
        button.startAnimation(pulse);
    }

    // Animation de suppression
    private void animateItemRemoval(View itemView, Runnable onComplete) {
        Animation fadeOut = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_down);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        itemView.startAnimation(fadeOut);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MealViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public void resetAnimation() {
        lastPosition = -1;
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName, tvDateTime, tvNotes;
        ImageButton btnEdit, btnDelete;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}