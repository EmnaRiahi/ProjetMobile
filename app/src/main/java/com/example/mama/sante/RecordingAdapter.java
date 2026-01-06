package com.example.mama.sante;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

    private List<File> recordings;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onPlayClick(File file);

        void onDeleteClick(File file);

        void onDownloadClick(File file);
    }

    private boolean isDarkMode = false;

    public RecordingAdapter(List<File> recordings, OnItemClickListener listener) {
        this.recordings = recordings;
        this.listener = listener;
    }

    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = recordings.get(position);
        holder.bind(file, listener, isDarkMode);
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDate;
        View btnPlay;
        ImageButton btnDelete;
        ImageButton btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecordingName);
            tvDate = itemView.findViewById(R.id.tvRecordingDate);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }

        public void bind(File file, OnItemClickListener listener, boolean isDarkMode) {
            tvName.setText(file.getName());

            Date lastModDate = new Date(file.lastModified());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvDate.setText(formatter.format(lastModDate));

            btnPlay.setOnClickListener(v -> listener.onPlayClick(file));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(file));
            btnDownload.setOnClickListener(v -> listener.onDownloadClick(file));

            // Apply Theme
            android.content.Context context = itemView.getContext();
            if (isDarkMode) {
                ((com.google.android.material.card.MaterialCardView) itemView)
                        .setCardBackgroundColor(
                                androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_card));
                tvName.setTextColor(
                        androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_text_primary));
                tvDate.setTextColor(
                        androidx.core.content.ContextCompat.getColor(context, R.color.sante_dark_text_secondary));
            } else {
                ((com.google.android.material.card.MaterialCardView) itemView)
                        .setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.white));
                tvName.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_primary));
                tvDate.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.grey_text));
            }
        }
    }
}
