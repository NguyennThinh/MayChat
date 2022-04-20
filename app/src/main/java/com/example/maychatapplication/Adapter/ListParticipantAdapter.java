package com.example.maychatapplication.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.Model.Participant;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListParticipantAdapter extends RecyclerView.Adapter<ListParticipantAdapter.ListParticipantViewHolder>{
    private List<Participant> arrParticipants;
    private String idGroup;

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    int role;
    public ListParticipantAdapter(List<Participant> arrParticipants, String idGroup, int role) {
        this.arrParticipants = arrParticipants;
        this.idGroup = idGroup;
        this.role = role;
    }

    @NonNull
    @Override
    public ListParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_participant, parent, false);
        return new ListParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListParticipantViewHolder holder, int position) {
        Participant participant = arrParticipants.get(position);
        if (participant.getRole() == 1){
            holder.positionGroup.setText("Trưởng nhóm");
        }else if(participant.getRole() ==2){
            holder.positionGroup.setText("Phó nhóm");
        }else {
            holder.positionGroup.setText("Thành viên");
        }

        loadUser(participant, holder);
    }


    @Override
    public int getItemCount() {
        return arrParticipants.size();
    }

    static class ListParticipantViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgUser;
        private TextView userName, positionGroup;
        private RelativeLayout layout_participant;
        public ListParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.img_user);
            userName = itemView.findViewById(R.id.user_name);
            positionGroup = itemView.findViewById(R.id.position_group);
            layout_participant = itemView.findViewById(R.id.layout_participant);
        }
    }
    private void loadUser(Participant participant, ListParticipantViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn : snapshot.getChildren()){
                    Users users = dn.getValue(Users.class);
                    if (participant.getId().equals(users.getId())){
                        holder.userName.setText(users.getFullName());
                        if (users.getAvatar().equals("default")){
                            holder.imgUser.setImageResource(R.drawable.ic_launcher_background);
                        }else {
                            Picasso.get().load(users.getAvatar()).into(holder.imgUser);
                        }


                        holder.layout_participant.setOnClickListener(view->{

                            if (role ==1){
                                if (participant.getRole()==2){
                                    showOptionAdmin2(view,participant);
                                }else if (participant.getRole() ==3){
                                    showOptionAdmin3(view,participant);
                                }else {
                                    System.out.println("Ko co quyen truy cap");
                                }
                            }else if (role == 2){
                                if (participant.getRole() ==3){
                                    showOptionAdmin3(view,participant);
                                }else {
                                    System.out.println("Ko co quyen truy cap");
                                }
                            }else {
                                System.out.println("Ko co quyen truy cap");
                            }

                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showOptionAdmin2(View view, Participant participant) {
        //Option edit
        String[] option = {"Chuyển nhóm trưởng","Xóa chức vụ" ,"Xóa thành viên"};

        //Dialog edit
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setItems(option, (dialogInterface, i) -> {
            switch (i){
                case 0:
                    setAdmin(participant, view);
                    break;
                case 1:
                    deleteManager(participant, view);

                    break;
                case 2:
                    deleteMember(participant, view);
                    break;

            }
        });
        builder.create().show();
    }



    private void showOptionAdmin3(View view, Participant participant) {
        //Option edit
        String[] option = {"Chuyển nhóm trưởng","Quản lý nhóm","Xóa thành viên"};

        //Dialog edit
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setItems(option, (dialogInterface, i) -> {
            switch (i){
                case 0:
                   setAdmin(participant, view);
                    break;
                case 1:
                    setManager(participant, view);

                    break;
                case 2:
                    deleteMember(participant, view);
                    break;

            }
        });
        builder.create().show();
    }


    private void deleteMember(Participant participant, View view) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        Query query= databaseReference.child(idGroup).child("participant").child(participant.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                        snapshot.getRef().removeValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setManager(Participant participant, View view) {

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                Query query= databaseReference.child(idGroup).child("participant").child(participant.getId()).orderByChild("id").equalTo(participant.getId());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("role",2);
                        snapshot.getRef().updateChildren(map);
                        System.out.println("ok");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
    private void setAdmin(Participant participant, View view) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        Query query= databaseReference.child(idGroup).child("participant").child(participant.getId()).orderByChild("id").equalTo(participant.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("role",1);
                snapshot.getRef().updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Groups");
        Query query1= databaseReference1.child(idGroup).child("participant").child(mUser.getUid());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("role",3);
                snapshot.getRef().updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void deleteManager(Participant participant, View view) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        Query query= databaseReference.child(idGroup).child("participant").child(participant.getId()).orderByChild("id").equalTo(participant.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("role",3);
                snapshot.getRef().updateChildren(map);
                System.out.println("ok");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
