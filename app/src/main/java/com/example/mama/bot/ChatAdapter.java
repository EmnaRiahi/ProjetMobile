package com.example.mama.bot;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mama.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<ChatMessage> list;

    public ChatAdapter(List<ChatMessage> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatMessage chat = list.get(position);
        holder.tvMsg.setText(chat.message);

        // --- C'EST ICI QU'ON GÈRE LE DESIGN DROITE / GAUCHE ---
        if (chat.sender.equals("USER")) {
            // Utilisateur -> Droite + Rose pâle
            holder.layout.setGravity(Gravity.END);
            holder.tvMsg.setBackgroundColor(Color.parseColor("#FFCDD2"));
        } else {
            // Bot -> Gauche + Blanc
            holder.layout.setGravity(Gravity.START);
            holder.tvMsg.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView tvMsg;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.chatLayout);
            tvMsg = itemView.findViewById(R.id.tvMessage);
        }
    }
}