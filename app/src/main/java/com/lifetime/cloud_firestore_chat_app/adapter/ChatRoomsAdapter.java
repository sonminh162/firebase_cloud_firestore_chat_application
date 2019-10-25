package com.lifetime.cloud_firestore_chat_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifetime.cloud_firestore_chat_app.R;
import com.lifetime.cloud_firestore_chat_app.model.ChatRoom;

import java.util.List;

public class ChatRoomsAdapter extends RecyclerView.Adapter<ChatRoomsAdapter.ChatRoomViewHolder> {

    public interface OnChatRoomClickListener {
        void onClick(ChatRoom chatRoom);
    }

    private OnChatRoomClickListener listener;

    private List<ChatRoom> chatRooms;

    public ChatRoomsAdapter(List<ChatRoom> chatRooms, OnChatRoomClickListener listener){
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        holder.bind(chatRooms.get(position));
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ChatRoom chatRoom;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_chat_room_name);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    listener.onClick(chatRoom);
                }
            });
        }

        public void bind(ChatRoom chatRoom){
            this.chatRoom = chatRoom;
            name.setText(chatRoom.getName());
        }
    }
}
