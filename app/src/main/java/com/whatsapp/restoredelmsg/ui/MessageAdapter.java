package com.whatsapp.restoredelmsg.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.whatsapp.restoredelmsg.R;
import com.whatsapp.restoredelmsg.data.MessageEntity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";

    private List<MessageEntity> messages = new ArrayList<>();

    public void setMessages(List<MessageEntity> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public List<MessageEntity> getMessages() {
        return messages;
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        MessageEntity msg = messages.get(position);
        holder.sender.setText(msg.sender);
        holder.text.setText(msg.text);
        holder.time.setText(DateFormat.getTimeInstance().format(msg.timestamp));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView sender, text, time;

        MessageViewHolder(View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.sender);
            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
        }
    }
}
