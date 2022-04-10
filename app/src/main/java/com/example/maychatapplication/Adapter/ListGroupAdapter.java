package com.example.maychatapplication.Adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.Model.Group;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.example.maychatapplication.Views.ChatGroupActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListGroupAdapter extends RecyclerView.Adapter<ListGroupAdapter.ListGroupViewHolder> implements Filterable {
    List<Group> arrGroup;
    List<Group> arrGroupOld;

    PreferenceManager preferenceManager;

    public ListGroupAdapter(List<Group> arrGroup) {
        this.arrGroup = arrGroup;
        this.arrGroupOld = arrGroup;

    }

    @NonNull
    @Override
    public ListGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_group, parent, false);
        return new ListGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListGroupViewHolder holder, int position) {
        Group group = arrGroup.get(position);
        Picasso.get().load(group.getGroupImage()).into(holder.imgGroup);
        holder.groupName.setText(group.getGroupName());
        holder.layoutGroup.setOnClickListener(view->{
            Intent intent = new Intent(view.getContext(), ChatGroupActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("group", group);
            intent.putExtras(bundle);

            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return arrGroup.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence s) {
                String query = s.toString();
                if (query.isEmpty()){
                    arrGroup = arrGroupOld;
                }else {
                    List<Group> mGroup = new ArrayList<>();
                    for (Group e: arrGroupOld){
                        if (e.getGroupName().toLowerCase().contains(query.toLowerCase())){
                            mGroup.add(e);
                        }
                    }
                    arrGroup = mGroup;
                }
                FilterResults results = new FilterResults();
                results.values = arrGroup;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                arrGroup = (List<Group>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    class ListGroupViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgGroup;
        private TextView groupName;
        private LinearLayout layoutGroup;
        public ListGroupViewHolder(@NonNull View itemView) {
            super(itemView);

            imgGroup = itemView.findViewById(R.id.item_avatar_group);
            groupName = itemView.findViewById(R.id.item_name_group);
            layoutGroup = itemView.findViewById(R.id.layoutGroup);
        }
    }
}
