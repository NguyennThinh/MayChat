package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.example.maychatapplication.Adapter.LayoutListWorkAdapter;
import com.example.maychatapplication.Model.Work;
import com.example.maychatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListWorkActivity extends AppCompatActivity {
    //Views
    RecyclerView list_work;
    //Declare firebase
    private FirebaseUser mUser;

    //Declare list
    private List<Work> arrWorks;

    //Adapter
    private LayoutListWorkAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_work);
        initUI();

        loadListWork();

    }


    private void initUI() {

        list_work = findViewById(R.id.list_work);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        arrWorks = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        list_work.setLayoutManager(layoutManager);
        list_work.addItemDecoration(itemDecoration);
    }


    private void loadListWork() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("WorkList");
        Query query = databaseReference.orderByChild("workCreateDate");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn : snapshot.getChildren()){
                    Work work = dn.getValue(Work.class);
                    if (work != null){
                        arrWorks.add(0,work);
                    }
                }
                adapter = new LayoutListWorkAdapter(arrWorks);
                list_work.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}