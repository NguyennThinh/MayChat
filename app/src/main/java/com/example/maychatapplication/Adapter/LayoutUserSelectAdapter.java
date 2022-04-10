package com.example.maychatapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

public class LayoutUserSelectAdapter extends RecyclerView.Adapter<LayoutUserSelectAdapter.ListEmployee> {
    List<Users> arrUsers;
    iAddMemberGroup addMemberGroup;

    private List<Users> arrUsersSelect = new ArrayList<>();

    public LayoutUserSelectAdapter(List<Users> arrUsers, iAddMemberGroup addMemberGroup) {
        this.arrUsers = arrUsers;
        this.addMemberGroup = addMemberGroup;
    }

    @NonNull
    @Override
    public ListEmployee onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_select_group, parent, false);
        return new ListEmployee(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListEmployee holder, int position) {
        Users users = arrUsers.get(position);
        if (users.getAvatar().equals("default")){
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background);
        }else {
            Picasso.get().load(users.getAvatar()).into(holder.imgAvatar);
        }
        holder.tvName.setText(users.getFullName());
        holder.tvPosition.setText(users.getPosition());
        holder.checkMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.checkMember.isChecked()) {
                    arrUsersSelect.add(users);

                } else {
                    arrUsersSelect.remove(users);
                }
                addMemberGroup.addMemberGroup(arrUsersSelect);
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrUsers.size();
    }


    class ListEmployee extends RecyclerView.ViewHolder {
        private CircleImageView imgAvatar;
        private TextView tvName, tvPosition;
        private CheckBox checkMember;

        public ListEmployee(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.user_select_group);
            tvName = itemView.findViewById(R.id.tvNameMember);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            checkMember = itemView.findViewById(R.id.checkMember);
        }
    }
}
