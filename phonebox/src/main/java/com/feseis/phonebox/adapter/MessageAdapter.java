package com.feseis.phonebox.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feseis.phonebox.R;
import com.feseis.phonebox.model.SmsMessage;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    public interface Listener {
        void onMarkRead(SmsMessage message);

        void onDelete(SmsMessage message);
    }

    private final List<SmsMessage> messages = new ArrayList<>();
    private final Listener listener;

    public MessageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void replaceAll(List<SmsMessage> items) {
        messages.clear();
        messages.addAll(items);
        Collections.reverse(messages);
        notifyDataSetChanged();
    }

    public void addNew(List<SmsMessage> items) {
        if (items.isEmpty()) {
            return;
        }
        for (SmsMessage item : items) {
            messages.add(0, item);
        }
        notifyItemRangeInserted(0, items.size());
    }

    public void markReadThrough(long lastId) {
        boolean changed = false;
        for (SmsMessage message : messages) {
            if (message.id <= lastId && !message.receiverRead) {
                message.receiverRead = true;
                changed = true;
            }
        }
        if (changed) {
            notifyDataSetChanged();
        }
    }

    public void removeById(long id) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).id == id) {
                messages.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    public int unreadCount() {
        int count = 0;
        for (SmsMessage message : messages) {
            if (!message.receiverRead) {
                count++;
            }
        }
        return count;
    }

    public long maxId() {
        long maxId = 0;
        for (SmsMessage message : messages) {
            if (message.id > maxId) {
                maxId = message.id;
            }
        }
        return maxId;
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SmsMessage message = messages.get(position);

        String sender = message.senderName;
        holder.tvSender.setText(sender == null || sender.isEmpty() ? message.senderPhone : sender);
        holder.tvTime.setText(message.displayTime());
        holder.tvContent.setText(message.content);
        holder.tvContent.setTypeface(null, message.receiverRead ? Typeface.NORMAL : Typeface.BOLD);
        holder.dotUnread.setVisibility(message.receiverRead ? View.GONE : View.VISIBLE);
        holder.tvType.setText("CODE".equals(message.messageType)
                ? R.string.type_code
                : R.string.type_text);

        holder.itemView.setOnClickListener(view -> listener.onMarkRead(message));
        holder.tvDelete.setOnClickListener(view -> listener.onDelete(message));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        final TextView tvSender;
        final TextView tvTime;
        final TextView tvContent;
        final TextView tvType;
        final TextView tvDelete;
        final View dotUnread;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tv_sender);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDelete = itemView.findViewById(R.id.tv_delete);
            dotUnread = itemView.findViewById(R.id.dot_unread);
        }
    }
}
