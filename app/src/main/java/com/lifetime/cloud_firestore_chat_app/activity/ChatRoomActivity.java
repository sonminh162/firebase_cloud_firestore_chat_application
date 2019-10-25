package com.lifetime.cloud_firestore_chat_app.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lifetime.cloud_firestore_chat_app.R;
import com.lifetime.cloud_firestore_chat_app.Repository.ChatRoomRepository;
import com.lifetime.cloud_firestore_chat_app.adapter.ChatsAdapter;
import com.lifetime.cloud_firestore_chat_app.model.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String CURRENT_USER_KEY = "CURRENT_USER_KEY";

    private String userId = "";

    private ChatRoomRepository chatRoomRepository;

    private EditText message;
    private ImageButton send;

    public static final String CHAT_ROOM_ID = "CHAT_ROOM_ID";
    public static final String CHAT_ROOM_NAME = "CHAT_ROOM_NAME";

    private String roomId = "";
    private String roomName = "";

    private RecyclerView chats;
    private ChatsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomId = extras.getString(CHAT_ROOM_ID, "");
            roomName = extras.getString(CHAT_ROOM_NAME, "");
        }

        if (getSupportActionBar() != null) {
            setTitle(roomName);
        }

        chatRoomRepository = new ChatRoomRepository(FirebaseFirestore.getInstance());

        userId = getCurrentUserKey();

        initUI();

        showChatMessages();
    }

    private String getCurrentUserKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(CURRENT_USER_KEY, "");
    }

    private void initUI() {
        message = findViewById(R.id.message_text);
        send = findViewById(R.id.send_message);
        chats = findViewById(R.id.chats);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        chats.setLayoutManager(manager);

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (message.getText().toString().isEmpty()) {
                    Toast.makeText(ChatRoomActivity.this, getString(R.string.error_empty_message), Toast.LENGTH_SHORT).show();
                } else {
                    addMessageToChatRoom();
                }
            }
        });

    }

    private void showChatMessages() {
        chatRoomRepository.getChats(roomId, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("ChatRoomActivity", "Listen failed", e);
                    return;
                }

                List<Chat> messages = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    messages.add(
                            new Chat(doc.getId(),
                                    doc.getString("chat_room_id"),
                                    doc.getString("sender_id"),
                                    doc.getString("message"),
                                    doc.getLong("sent"))
                    );
                }

                adapter = new ChatsAdapter(messages,userId);
                chats.setAdapter(adapter);
            }
        });
    }



    private void addMessageToChatRoom() {
        String chatMessage = message.getText().toString();
        message.setText("");
        send.setEnabled(false);
        chatRoomRepository.addMessageToChatRoom(
                roomId,
                userId,
                chatMessage,
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        send.setEnabled(true);
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        send.setEnabled(true);
                        Toast.makeText(ChatRoomActivity.this, getString(R.string.error_message_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
