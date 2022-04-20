package com.example.maychatapplication.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.Model.Work;
import com.example.maychatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LayoutListWorkAdapter extends RecyclerView.Adapter<LayoutListWorkAdapter.ListWorkHolder> {
    private List<Work> arrWorks;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    public LayoutListWorkAdapter(List<Work> arrWorks) {
        this.arrWorks = arrWorks;
    }

    @NonNull
    @Override
    public ListWorkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_work, parent, false);
        return new ListWorkHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListWorkHolder holder, int position) {
        Work work = arrWorks.get(position);
        holder.workName.setText(work.getWorkName());

        loadLeader(holder, work);
        holder.workTime.setText("Thời gian: Từ: "+work.getWorkStart() +" đến: "+work.getWorkEnd());

        if (work.getWorkStatus() ==1){
            holder.workStatus.setImageResource(R.drawable.ic_error);
            holder.workComplete.setVisibility(View.GONE);

        }else if (work.getWorkStatus()==2){
            holder.workStatus.setImageResource(R.drawable.ic_check);
            holder.workComplete.setText(work.getDateComplete());

        }else {
            holder.workStatus.setImageResource(R.drawable.ic_cancle);
            holder.workComplete.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        return arrWorks.size();
    }

    static class ListWorkHolder extends RecyclerView.ViewHolder {
        private TextView workName, leader, workTime,  workComplete;
        private ImageView workStatus;
        public ListWorkHolder(@NonNull View itemView) {
            super(itemView);
            workName = itemView.findViewById(R.id.work_name);
            leader = itemView.findViewById(R.id.leader);
            workTime = itemView.findViewById(R.id.work_time);
            workStatus = itemView.findViewById(R.id.work_status);
            workComplete = itemView.findViewById(R.id.work_complete);
        }
    }
    private void loadLeader(ListWorkHolder holder, Work work) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(work.getLeader()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Users users = snapshot.getValue(Users.class);
                    holder.leader.setText("Người phụ trách: "+users.getFullName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
