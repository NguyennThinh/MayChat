package com.example.maychatapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LayoutUserSelectGroupAdapter extends RecyclerView.Adapter<LayoutUserSelectGroupAdapter.ListUserSelect>{
    List<Users> arrUsers;

    public LayoutUserSelectGroupAdapter(List<Users> arrUsers) {
        this.arrUsers = arrUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListUserSelect onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_select, parent, false);
        return new ListUserSelect(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListUserSelect holder, int position) {
        Users users = arrUsers.get(position);

        if (users.getAvatar().equals("default")){
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background);
        }else {
            Picasso.get().load(users.getAvatar()).into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return arrUsers.size();
    }

    class ListUserSelect extends RecyclerView.ViewHolder {
        private CircleImageView imgAvatar;
        private ImageView imgRemove;
        public ListUserSelect(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.user_select);
            imgRemove = itemView.findViewById(R.id.remove_user_select);
        }
    }
}
