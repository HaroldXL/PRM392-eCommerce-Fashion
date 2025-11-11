package com.example.prm392_finalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_RECIPIENT = 1;
    private static final int TYPE_SENDER = 2;
    private final List<ChatMessage> messages;
    private final int currentUserId;

    public ChatMessageAdapter(List<ChatMessage> messages, int currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.isUser(currentUserId)) {
            return TYPE_SENDER; // right side (blue bubble)
        } else {
            return TYPE_RECIPIENT; // left side (gray bubble)
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_RECIPIENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_recepient, parent, false);
            return new RecepientViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sender, parent, false);
            return new SenderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof RecepientViewHolder) {
            ((RecepientViewHolder) holder).tvMessage.setText(msg.getChatContent());
        } else if (holder instanceof SenderViewHolder) {
            ((SenderViewHolder) holder).tvMessage.setText(msg.getChatContent());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    // --- ViewHolders ---
    static class RecepientViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        RecepientViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        SenderViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
