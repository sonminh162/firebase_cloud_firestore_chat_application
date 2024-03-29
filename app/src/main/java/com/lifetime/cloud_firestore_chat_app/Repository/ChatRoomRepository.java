package com.lifetime.cloud_firestore_chat_app.Repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ChatRoomRepository {
    private static final String TAG = "ChatRoomRepo";

    private FirebaseFirestore db;

    public ChatRoomRepository(FirebaseFirestore db){
        this.db = db;
    }

    public void createRoom(String name,
                           final OnSuccessListener<DocumentReference> successCallback,
                           final OnFailureListener failureCallback){
        Map<String,Object> room = new HashMap<>();
        room.put("name",name);
        db.collection("rooms")
            .add(room)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    successCallback.onSuccess(documentReference);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    failureCallback.onFailure(e);
                }
            });

    }

    public void getRooms(EventListener<QuerySnapshot> listener){
        db.collection("rooms")
                .orderBy("name")
                .addSnapshotListener(listener);
    }

    public void addMessageToChatRoom(String roomId,
                                     String senderId,
                                     String message,
                                     final OnSuccessListener<DocumentReference> successCallback,
                                     final OnFailureListener failureCallback) {
        Map<String,Object> chat = new HashMap<>();
        chat.put("chat_room_id",roomId);
        chat.put("sender_id",senderId);
        chat.put("message",message);
        chat.put("sent",System.currentTimeMillis());

        db.collection("chats")
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        successCallback.onSuccess(documentReference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       failureCallback.onFailure(e);
                    }
                });
    }

    public void getChats(String roomId,EventListener<QuerySnapshot> listener){
        db.collection("chats")
                .whereEqualTo("chat_room_id",roomId)
                .orderBy("sent", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }
}
