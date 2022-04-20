package com.example.maychatapplication.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.example.maychatapplication.Adapter.ListChatAdapter;
import com.example.maychatapplication.Model.ChatList;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.example.maychatapplication.Views.CreateGroupActivity;
import com.example.maychatapplication.Views.CreateWorkActivity;
import com.example.maychatapplication.Views.ListWorkActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ListChatFragment extends Fragment {
    //Views
    RecyclerView list_chat;

    //Adapter
    ListChatAdapter adpter;
    //Declare Firebase
    FirebaseUser mUser;

    //Declare list
    List<ChatList> arrListIdChat;

    private Users users;
    private PreferenceManager preferenceManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_chat, container, false);

        initUI(view);

        loadListCHat();

        return view;
    }


    private void initUI(View view) {
        list_chat = view.findViewById(R.id.display_list_chat);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        arrListIdChat= new ArrayList<>();

        preferenceManager = new PreferenceManager(getContext());
        String value = preferenceManager.getString("users");
        Gson gson = new Gson();
        users = gson.fromJson(value, Users.class);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        list_chat.setLayoutManager(layoutManager);
        list_chat.addItemDecoration(itemDecoration);
    }

    private void loadListCHat() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(mUser.getUid());
        //get list id user đã chat
        Query query = databaseReference.orderByChild("timeSend");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrListIdChat.clear();
                for (DataSnapshot dn : snapshot.getChildren()) {
                    ChatList list = dn.getValue(ChatList.class);
                        arrListIdChat.add(0, list);
                }

                adpter =new ListChatAdapter(arrListIdChat);
                list_chat.setAdapter(adpter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.status_bar,menu);
        menu.findItem(R.id.create_group).setVisible(false);
        menu.findItem(R.id.search).setVisible(false);
        if (!users.getPosition().equals("CEO")){
            menu.findItem(R.id.create_work).setVisible(false);
            menu.findItem(R.id.list_work_create).setVisible(false);
        }else {
            menu.findItem(R.id.create_work).setVisible(true);
            menu.findItem(R.id.list_work_create).setVisible(true);
        }
        menu.findItem(R.id.list_my_work).setIcon(R.drawable.ic_list);
        menu.findItem(R.id.create_work).setIcon(R.drawable.ic_menu_work);
        menu.findItem(R.id.list_work_create).setIcon(R.drawable.ic_list);

        super.onCreateOptionsMenu(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.create_group:
                startActivity(new Intent(getContext(), CreateGroupActivity.class));
                break;
            case R.id.create_work:
                startActivity(new Intent(getContext(), CreateWorkActivity.class));
                break;
            case R.id.list_work_create:
                startActivity(new Intent(getContext(), ListWorkActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}