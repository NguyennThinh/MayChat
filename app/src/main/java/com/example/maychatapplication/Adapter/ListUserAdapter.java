package com.example.maychatapplication.Adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Views.ChatEmployeeActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.UsersHolder> implements Filterable {
    List<Users> arrUsers;
    List<Users> arrUsersOld;
    public ListUserAdapter(List<Users> arrUsers) {
        this.arrUsers = arrUsers;
        this.arrUsersOld = arrUsers;
    }

    @NonNull
    @Override
    public UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_employee, parent, false);
        return new UsersHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersHolder holder, int position) {
        Users users = arrUsers.get(position);
        if (users.getAvatar().equals("default")){
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background);
        }else {
            Picasso.get().load(users.getAvatar()).into(holder.imgAvatar);
        }
        holder.EmployeeName.setText(users.getFullName());
        holder.Position.setText(users.getPosition());
        holder.itemListEmployee.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ChatEmployeeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("users", users);
            intent.putExtras(bundle);
            view.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return arrUsers.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence s) {
                String query = s.toString();
                if (query.isEmpty()){
                    arrUsers = arrUsersOld;
                }else {
                    List<Users> users = new ArrayList<>();
                    for (Users u: arrUsersOld){
                        if (u.getFullName().toLowerCase().contains(query.toLowerCase())){
                            users.add(u);
                        }
                    }
                    arrUsers = users;
                }
                FilterResults results = new FilterResults();
                results.values = arrUsers;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                arrUsers = (List<Users>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    static class UsersHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgAvatar;
        private TextView EmployeeName;
        private TextView Position;
        private RelativeLayout itemListEmployee;
        public UsersHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.list_employee_avatar);
            EmployeeName = itemView.findViewById(R.id.list_employee_name);
            Position = itemView.findViewById(R.id.list_employee_position);
            itemListEmployee = itemView.findViewById(R.id.item_list_employee);
        }
    }
}
