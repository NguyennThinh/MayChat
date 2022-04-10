package com.example.maychatapplication.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import android.widget.Toast;

import com.example.maychatapplication.Adapter.ListGroupAdapter;
import com.example.maychatapplication.Model.Group;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Views.CreateGroupActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ListGroupFragment extends Fragment {
    private RecyclerView listGroup;
    private ListGroupAdapter adapter;
    private List<Group> arrGroup;

    FirebaseUser mUser ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_group, container, false);
        listGroup = view.findViewById(R.id.list_group);
        arrGroup = new ArrayList<>();

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        listGroup.setLayoutManager(layoutManager);
        listGroup.addItemDecoration(itemDecoration);

        loadListGroup();
        return view;
    }
    private void loadListGroup() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrGroup.clear();
                for (DataSnapshot dn : snapshot.getChildren()){

                    if (dn.child("participant").child(mUser.getUid()).exists()){
                        Group group = new Group(dn.child("groupID").getValue()+"",dn.child("groupName").getValue()+"",dn.child("description").getValue()+"",
                                dn.child("groupImage").getValue()+"",dn.child("userCreate").getValue()+"",dn.child("createDate").getValue()+"");
                        arrGroup.add(group);


                    }
                }
                adapter = new ListGroupAdapter(arrGroup);
                listGroup.setAdapter(adapter);

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
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(adapter != null){
                    Filter filter = adapter.getFilter();
                    filter.filter(newText);
                }

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.create_group:
                     startActivity(new Intent(getContext(), CreateGroupActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}