package com.example.prm392_finalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Chat;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Chat chat);
    }

    public ChatAdapter(List<Chat> chatList, OnItemClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bind(chat, listener);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tvCustomerName, tvLastMessage, tvTimestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(final Chat chat, final OnItemClickListener listener) {
            tvCustomerName.setText(chat.getCustomer().getFullName());
            tvLastMessage.setText(chat.getChatContents().get(0).getChatContent());
            tvTimestamp.setText(chat.getChatContents().get(0).getSentAt());

            itemView.setOnClickListener(v -> listener.onItemClick(chat));
        }
    }

    // Optional: method to update the list (for pagination)
    public void updateList(List<Chat> newList) {
        chatList.clear();
        chatList.addAll(newList);
        notifyDataSetChanged();
    }
}

