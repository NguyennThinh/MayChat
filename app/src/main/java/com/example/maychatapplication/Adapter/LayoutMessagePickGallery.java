package com.example.maychatapplication.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LayoutMessagePickGallery extends RecyclerView.Adapter<LayoutMessagePickGallery.PickViewHolder> {
    private List<Uri> arrUri;
    private String messageType;

    public LayoutMessagePickGallery(List<Uri> arrUri, String messageType) {
        this.arrUri = arrUri;
        this.messageType = messageType;
    }

    @NonNull
    @Override
    public PickViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mesage_select_gallery, parent, false);
        return new PickViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickViewHolder holder, int position) {
        Uri uri = arrUri.get(position);
        holder.nameFile.setText(getFileName(uri, holder.itemView.getContext()));

        if (messageType.equals("image")){
            holder.imgMessage.setVisibility(View.VISIBLE);
            holder.videoMessage.setVisibility(View.GONE);
            holder.imgPDF.setVisibility(View.GONE);

            holder.imgMessage.setImageURI(uri);
        }else if (messageType.equals("video")){
            holder.imgMessage.setVisibility(View.GONE);
            holder.videoMessage.setVisibility(View.VISIBLE);
            holder.imgPDF.setVisibility(View.GONE);
            holder.videoMessage.setVideoURI(uri);
        }else {
                holder.imgMessage.setVisibility(View.GONE);
                holder.videoMessage.setVisibility(View.GONE);
                holder.imgPDF.setVisibility(View.VISIBLE);
        }
        holder.imgRemove.setOnClickListener(view -> {
            arrUri.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return arrUri.size();
    }

    static class PickViewHolder extends RecyclerView.ViewHolder {
        private TextView nameFile;
        private ImageView imgMessage, imgPDF, imgRemove;
        private VideoView videoMessage;
        public PickViewHolder(@NonNull View itemView) {
            super(itemView);
            nameFile = itemView.findViewById(R.id.select_file_name);
            imgMessage = itemView.findViewById(R.id.image_select_gallery);
            videoMessage = itemView.findViewById(R.id.video_select_gallery);
            imgPDF = itemView.findViewById(R.id.img_pdf);
            imgRemove = itemView.findViewById(R.id.remove_message_select);
        }
    }

    @SuppressLint("Range")
    String getFileName(Uri data, Context context) {
        String res = null;
        if (data.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(data, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    res = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
                ;
            }
            if (res == null) {
                res = data.getPath();
                int cut = res.lastIndexOf("/");
                if (cut != -1) {
                    res = res.substring(cut + 1);
                }
            }
        }
        return res;
    }

}
