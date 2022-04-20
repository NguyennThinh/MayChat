package com.example.maychatapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.Interface.iAddMemberGroup;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LayouLeaderSelectAdapter extends RecyclerView.Adapter<LayouLeaderSelectAdapter.ListLeader> {
    List<Users> arrUsers;
    iAddMemberGroup addMemberGroup;


    public LayouLeaderSelectAdapter(List<Users> arrUsers, iAddMemberGroup addMemberGroup) {
        this.arrUsers = arrUsers;
        this.addMemberGroup = addMemberGroup;
    }

    @NonNull
    @Override
    public ListLeader onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_leader_work, parent, false);
        return new ListLeader(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListLeader holder, int position) {
        Users users = arrUsers.get(position);
        if (users.getAvatar().equals("default")){
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background);
        }else {
            Picasso.get().load(users.getAvatar()).into(holder.imgAvatar);
        }
        holder.tvName.setText(users.getFullName());
        holder.tvPosition.setText("Chức vụ: "+users.getPosition());
        holder.imgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addMemberGroup.addLeader(users);

            }
        });


    }

    @Override
    public int getItemCount() {
        return arrUsers.size();
    }

    static class ListLeader extends RecyclerView.ViewHolder {
        private CircleImageView imgAvatar;
        private TextView tvName, tvPosition;
        private ImageView imgSubmit;

        public ListLeader(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.img_leader);
            tvName = itemView.findViewById(R.id.name_leader);
            tvPosition = itemView.findViewById(R.id.leader_position);
            imgSubmit = itemView.findViewById(R.id.img_submit);
        }
    }
}
