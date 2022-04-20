package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maychatapplication.Adapter.AddParticipantAdapter;
import com.example.maychatapplication.Interface.iAddMemberGroup;
import com.example.maychatapplication.Model.Participant;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddParticipantActivity extends AppCompatActivity implements iAddMemberGroup {
    private RecyclerView rcv_list_friend;
    private TextView submit;

    private List<String> arrIdUser;
    List<Users> arrUsers, mUsers;
    private FirebaseUser mUser;
    private String groupID;
    private List<Participant> arrMember;

    private AddParticipantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participant);


        rcv_list_friend = findViewById(R.id.list_user);
        submit = findViewById(R.id.submit);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        groupID = getIntent().getStringExtra("group_id");

        arrUsers = new ArrayList<>();
        arrIdUser = new ArrayList<>();
        arrMember = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        rcv_list_friend.setLayoutManager(layoutManager);
        rcv_list_friend.addItemDecoration(itemDecoration);


        loadUser();

        submit.setOnClickListener(view->{
            addParticipant();
            this.finish();
        });
    }

    private void loadUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrUsers.clear();
                for (DataSnapshot dn : snapshot.getChildren()){
                    Users employee = dn.getValue(Users.class);

                    if (employee != null && !employee.getId().equals(mUser.getUid())){
                        arrUsers.add(employee);
                    }
                }
           //     LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

                adapter = new AddParticipantAdapter(arrUsers,groupID, AddParticipantActivity.this);
                rcv_list_friend.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addParticipant() {
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Groups")
                .child(groupID);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (Participant userGroup : arrMember){
                    databaseReference1.child("participant").child(userGroup.getId()).setValue(userGroup);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public void addMemberGroup(List<Users> arrUser) {
        mUsers = arrUser;
        for (Users e: mUsers){
            arrMember.add(new Participant(e.getId(), 3, String.valueOf(System.currentTimeMillis())));
        }
    }

    @Override
    public void addLeader(Users users) {

    }
}