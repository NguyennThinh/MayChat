package com.example.maychatapplication.Adapter;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maychatapplication.Model.GroupMessage;
import com.example.maychatapplication.Model.SingleMessage;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LayoutMessageUserAdapter extends RecyclerView.Adapter<LayoutMessageUserAdapter.MessageHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    List<SingleMessage> arrMessage;

    private String URL_USER;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();

    public LayoutMessageUserAdapter(List<SingleMessage> arrMessage, String URL_USER) {
        this.arrMessage = arrMessage;
        this.URL_USER = URL_USER;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_message_user_right, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_message_user_left, parent, false);
        }
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        SingleMessage message = arrMessage.get(position);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM HH:mm");
        Long date = Long.valueOf(message.getTime());

        holder.messageSendTime.setText(format.format(date));

        if (URL_USER.equals("default")) {
            holder.imgUserSender.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Picasso.get().load(URL_USER).into(holder.imgUserSender);
        }

        String messageType = message.getType();

        if (messageType.equals("text")) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageVideo.setVisibility(View.GONE);
            holder.layoutMessageFile.setVisibility(View.GONE);

            holder.messageText.setText(message.getMessage());
        } else if (messageType.equals("image")) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageVideo.setVisibility(View.GONE);
            holder.layoutMessageFile.setVisibility(View.GONE);

            Picasso.get().load(message.getMessage()).into(holder.messageImage);

        } else if (messageType.equals("video")) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageVideo.setVisibility(View.VISIBLE);
            holder.layoutMessageFile.setVisibility(View.GONE);

            holder.messageVideo.setVideoURI(Uri.parse(message.getMessage()));
            MediaController mediaController = new MediaController(holder.itemView.getContext());
            holder.messageVideo.setMediaController(mediaController);
            mediaController.setAnchorView(holder.messageVideo);
        } else {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageVideo.setVisibility(View.GONE);
            holder.layoutMessageFile.setVisibility(View.VISIBLE);

            holder.fileName.setText(message.getFileName());
            Long size = Long.parseLong(message.getFileSize());
            holder.fileSize.setText(getFileSize(size));


        }

        if (position == arrMessage.size() - 1) {
            if (message.isSeen()) {
                holder.messageStatus.setText("Đã xem");
            } else {
                holder.messageStatus.setText("Đã nhân");

            }
        } else {
            holder.messageStatus.setVisibility(View.GONE);
        }


        holder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DeleteMessage(v, message);
                return false;
            }
        });
        holder.messageImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DeleteMessage(v, message);
                return false;
            }
        });
        holder.messageVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DeleteMessage(v, message);
                return false;
            }
        });

        holder.messageImage.setOnClickListener(view -> {
            showDialogMessage(view, message);
        });
        holder.messageVideo.setOnClickListener(view->{
            showDialogMessage(view, message);
        });

    }


    @Override
    public int getItemCount() {
        return arrMessage.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (arrMessage.get(position).getIdSender().equals(mUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }


    static class MessageHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgUserSender;
        private TextView messageText, messageSendTime, messageStatus, fileName, fileSize;
        private ImageView messageImage;
        private VideoView messageVideo;
        private RelativeLayout layoutMessageFile;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            imgUserSender = itemView.findViewById(R.id.img_user_sender);
            messageText = itemView.findViewById(R.id.message_text);
            messageSendTime = itemView.findViewById(R.id.message_send_time);
            messageStatus = itemView.findViewById(R.id.status_message);
            fileName = itemView.findViewById(R.id.message_file_name);
            fileSize = itemView.findViewById(R.id.message_file_size);
            messageImage = itemView.findViewById(R.id.message_image);
            messageVideo = itemView.findViewById(R.id.message_video);
            layoutMessageFile = itemView.findViewById(R.id.layout_message_pdf);

        }
    }

    private void DeleteMessage(View view, SingleMessage message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Xóa tin nhắn");
        builder.setMessage("Bạn muốn xóa tin nhắn");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String timeSend = message.getTime();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Message");
                Query query = databaseReference.orderByChild("time").equalTo(timeSend);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dn : snapshot.getChildren()) {
                            if (dn.child("idSender").getValue().equals(mUser.getUid())) {
                                //    dn.getRef().removeValue();
                                if (dn.child("type").getValue().equals("text")) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("message", "Tin nhắn này đã bị xóa");
                                    dn.getRef().updateChildren(hashMap);

                                } else {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("message", "Tin nhắn này đã bị xóa");
                                    hashMap.put("type", "text");
                                    dn.getRef().updateChildren(hashMap);
                                }
                            } else {
                                Toast.makeText(view.getContext(), "Bạn không thể xóa tin nhắn này", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void showDialogMessage(View view, SingleMessage message) {
        Dialog dialog = new Dialog(view.getContext(), android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.custom_message_dialog);
        ImageView imgBack = dialog.findViewById(R.id.img_back);
        ImageView imgMessage = dialog.findViewById(R.id.image_message_zoom_in);
        VideoView videoMessage = dialog.findViewById(R.id.video_message_zoom_in);
        if (message.getType().equals("image")) {
            imgMessage.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getMessage()).into(imgMessage);
        } else {
            videoMessage.setVisibility(View.VISIBLE);
            MediaController mediaController = new MediaController(videoMessage.getContext());
            videoMessage.setVideoPath(message.getMessage());

           videoMessage.setMediaController(mediaController);
           mediaController.setAnchorView(videoMessage);
           videoMessage.start();
        }
        imgBack.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }
}
