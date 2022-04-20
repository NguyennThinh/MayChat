package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.maychatapplication.Adapter.LayouLeaderSelectAdapter;
import com.example.maychatapplication.Adapter.ListUserAdapter;
import com.example.maychatapplication.Interface.iAddMemberGroup;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class LeaderWorkActivity extends AppCompatActivity  {
    //Views
    private RecyclerView list_leader;

    //Adapter
    private LayouLeaderSelectAdapter adapter;

    private Users users;
    private List<Users> arrUsers;
    //Declare firebase
    private FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_work);

        list_leader = findViewById(R.id.list_leader_work);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        arrUsers = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        list_leader.setLayoutManager(layoutManager);
        list_leader.addItemDecoration(itemDecoration);

        loadListUsers();
    }

    private void loadListUsers() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrUsers.clear();
                for (DataSnapshot dn : snapshot.getChildren()){
                    Users users = dn.getValue(Users.class);

                    assert users != null;
                    if (!users.getId().equals(mUser.getUid())){
                        arrUsers.add(users);
                    }
                }
                adapter = new LayouLeaderSelectAdapter(arrUsers, new iAddMemberGroup() {
                    @Override
                    public void addMemberGroup(List<Users> arrEmployees) {

                    }

                    @Override
                    public void addLeader(Users users) {
                        if (users !=null){
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("leader", users);
                            intent.putExtras(bundle);
                            setResult(RESULT_OK, intent);

                            finish();
                        }
                    }
                });
                list_leader.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}