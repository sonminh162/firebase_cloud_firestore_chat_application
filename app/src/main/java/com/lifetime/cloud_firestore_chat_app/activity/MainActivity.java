package com.lifetime.cloud_firestore_chat_app.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lifetime.cloud_firestore_chat_app.R;
import com.lifetime.cloud_firestore_chat_app.Repository.AuthenticationRepository;
import com.lifetime.cloud_firestore_chat_app.Repository.ChatRoomRepository;
import com.lifetime.cloud_firestore_chat_app.adapter.ChatRoomsAdapter;
import com.lifetime.cloud_firestore_chat_app.model.ChatRoom;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CURRENT_USER_KEY = "CURRENT_USER_KEY";

    private FloatingActionButton createRoom;

    AuthenticationRepository authentication;

    ChatRoomRepository chatRoomRepository;

    private RecyclerView chatRooms;
    private ChatRoomsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatRoomRepository = new ChatRoomRepository(FirebaseFirestore.getInstance());

        authentication = new AuthenticationRepository(FirebaseFirestore.getInstance());

        createRoom = findViewById(R.id.create_room);

        initUI();

        authenticate();

        getChatRooms();
    }

    private void getChatRooms(){
        chatRoomRepository.getRooms(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!= null){
                    Log.e("MainActivity","Listen failed",e);
                    return;
                }

                List<ChatRoom> rooms = new ArrayList<>();
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    rooms.add(new ChatRoom(doc.getId(),doc.getString("name")));
                }

                adapter = new ChatRoomsAdapter(rooms,listener);
                chatRooms.setAdapter(adapter);
            }
        });
    }

    ChatRoomsAdapter.OnChatRoomClickListener listener = new ChatRoomsAdapter.OnChatRoomClickListener(){
        @Override
        public void onClick(ChatRoom chatRoom) {
            Intent intent = new Intent(MainActivity.this,ChatRoomActivity.class);
            intent.putExtra(ChatRoomActivity.CHAT_ROOM_ID, chatRoom.getId());
            intent.putExtra(ChatRoomActivity.CHAT_ROOM_NAME, chatRoom.getName());
            startActivity(intent);
        }
    };

    private void initUI(){
        chatRooms = findViewById(R.id.rooms);
        chatRooms.setLayoutManager(new LinearLayoutManager(this));
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainActivity","Launch create a room screen");
                Intent intent = new Intent(MainActivity.this,CreateRoomActivity.class);
                startActivity(intent);
            }
        });
    }

    private String getCurrentUserKey(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(CURRENT_USER_KEY,"");
    }

    private void saveCurrentUserKey(String key){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CURRENT_USER_KEY, key);
        editor.apply();
    }

    private void authenticate(){
        String currentUserKey = getCurrentUserKey();
        if(currentUserKey.isEmpty()){
            authentication.createNewUser(
                    new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            saveCurrentUserKey(documentReference.getId());
                            Toast.makeText(MainActivity.this, "New user created", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Error creating user. Check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            authentication.login(
                    currentUserKey,
                    new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(MainActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Error signing in. Check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }
}
