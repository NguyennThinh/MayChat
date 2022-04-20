package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.maychatapplication.Adapter.LayoutMessageGroupAdapter;
import com.example.maychatapplication.Adapter.LayoutMessagePickGallery;


import com.example.maychatapplication.Adapter.ListUserAdapter;
import com.example.maychatapplication.Fcm.ApiClient;
import com.example.maychatapplication.Model.Group;
import com.example.maychatapplication.Model.GroupMessage;
import com.example.maychatapplication.Model.Participant;
import com.example.maychatapplication.Model.SingleMessage;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatGroupActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    //Views
    private ImageView imgSelectMessage, imgAudioCall, imgBackActivity, imgCamera, imgSend, imgMenu;
    private EditText inputMessage;
    private RecyclerView layoutDisplayMessage, layoutDisplayMessageSelect;
    private CircleImageView imgGroup;
    private TextView groupName, sumParticipant;
    private RelativeLayout layoutProgress;
    private ProgressBar loading;
    private TextView loadingView;

    //adapter
    LayoutMessageGroupAdapter adapter;
    LayoutMessagePickGallery pickGallery;

    //Declare Firebase
    private FirebaseUser mUser;
    private DatabaseReference seenRef;
    private ValueEventListener seenListener;

    //Declare group receiver;
    private Group groupReceiver;

    private Users userSender;

    private List<GroupMessage> arrMessage;
    private List<String>  arrUserID;

    //List message type pick gallery
    private List<Uri> arrImageUri;
    private List<Uri> arrVideoUri;
    private List<Uri> arrFileUri;

    private String messageType;
    private String selectMessage;
    String timeSend = String.valueOf(System.currentTimeMillis());

    //Declare request code pick image gallery
    private static final int REQUEST_PICK_IMAGE_CODE = 100;
    private static final int REQUEST_PERMISSION_CODE_CAMERA = 101;
    private static final int REQUEST_PERMISSION_CODE_PICK_GALLERY = 102;
    private static final int REQUEST_CAMERA_PICTURE_CODE = 103;
    private static final int REQUEST_PICK_PDF_CODE = 104;
    private static final int REQUEST_PICK_VIDEO_CODE = 105;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);

        initUI();
        loadInfoGroup();
        displayMessage();
        seenMessage();
        //Handle views click
        imgSelectMessage.setOnClickListener(this::showPopup);


        imgCamera.setOnClickListener(view -> {
            PermissionCameraPicture();
        });

        imgBackActivity.setOnClickListener(view -> {
            finish();
        });

        imgMenu.setOnClickListener(view->{
            Intent intent = new Intent(this, InformationGroupActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("my_group", groupReceiver);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        imgSend.setOnClickListener(view -> {
            String msg = inputMessage.getText().toString();


            if (!msg.equals("")) {
                messageType = "text";
                GroupMessage message = new GroupMessage(mUser.getUid() , msg, timeSend, messageType,"default","default", false);
                sendMessage(message);
                layoutDisplayMessage.scrollToPosition(arrMessage.size() - 1);
                getTokenUserGroup(message);

            }else {
                if (arrImageUri.size() >0){
                    messageType = "image";
                    sendImageMessage();
                }else if (arrVideoUri.size()>0){
                    messageType = "video";
                    sendVideoMessage();
                }else if (arrFileUri.size()>0){
                    messageType = "pdf";
                    sendFileMessage();
                }
            }
            inputMessage.setText("");
        });
    }



    private void initUI() {
        imgSelectMessage = findViewById(R.id.select_message_type);
        imgBackActivity = findViewById(R.id.img_back);
        imgSelectMessage = findViewById(R.id.select_message_type);
        imgSend = findViewById(R.id.send_message);
        imgCamera = findViewById(R.id.select_camera);
        inputMessage = findViewById(R.id.input_message);
        imgGroup = findViewById(R.id.img_group_receiver);
        imgMenu = findViewById(R.id.img_menu_group);
        groupName = findViewById(R.id.tv_name_group_receiver);
        sumParticipant = findViewById(R.id.tv_sum_participant);
        layoutProgress = findViewById(R.id.layout_progress);
        loading = findViewById(R.id.loading);
        loadingView = findViewById(R.id.loading_view);

        layoutDisplayMessage = findViewById(R.id.layout_display_message);
        layoutDisplayMessageSelect = findViewById(R.id.layout_message_select);


        mUser = FirebaseAuth.getInstance().getCurrentUser();
        groupReceiver = (Group) getIntent().getExtras().get("group");

        arrImageUri = new ArrayList<>();
        arrVideoUri = new ArrayList<>();
        arrFileUri = new ArrayList<>();
        arrMessage = new ArrayList<>();
        arrUserID = new ArrayList<>();

        PreferenceManager preferenceManager = new PreferenceManager(this);
        String user = preferenceManager.getString("users");
        Gson gson = new Gson();
        userSender = gson.fromJson(user, Users.class);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutDisplayMessage.setLayoutManager(layoutManager);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        layoutDisplayMessageSelect.setLayoutManager(linearLayoutManager);
    }

    private void loadInfoGroup() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupReceiver.getGroupID()).child("participant")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Participant> arrUserGroups = new ArrayList<>();
                        for (DataSnapshot dn : snapshot.getChildren()) {
                            Participant participant = dn.getValue(Participant.class);
                         //  arrUserGroups.add(new Participant(dn.child("id").getValue() + "", dn.child("role").getValue(), dn.child("partDate").getValue() + ""));
                            arrUserGroups.add(participant);
                            arrUserID.add(dn.child("id").getValue().toString());

                        }
                        groupName.setText(groupReceiver.getGroupName());
                        Picasso.get().load(groupReceiver.getGroupImage()).into(imgGroup);
                        sumParticipant.setText(arrUserGroups.size() + " Thành viên");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void displayMessage() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupReceiver.getGroupID()).child("Message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrMessage.clear();
                for (DataSnapshot dn : snapshot.getChildren()){
                    GroupMessage message = dn.getValue(GroupMessage.class);

                    arrMessage.add(message);

                }

                adapter = new LayoutMessageGroupAdapter(arrMessage, groupReceiver.getGroupID());
                layoutDisplayMessage.setAdapter(adapter);
                layoutDisplayMessage.scrollToPosition(arrMessage.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void seenMessage(){
        seenRef = FirebaseDatabase.getInstance().getReference("Groups");
        seenListener = seenRef.child(groupReceiver.getGroupID()).child("Message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn : snapshot.getChildren()){
                    GroupMessage  message = dn.getValue(GroupMessage.class);
                    if (message != null){
                        if (!message.getIdSender().equals(mUser.getUid())){
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("seen", true);
                            dn.getRef().updateChildren(map);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendMessage(GroupMessage message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupReceiver.getGroupID()).child("Message").child(timeSend).setValue(message);


        HashMap<String, Object> chatlist = new HashMap<>();
        chatlist.put("id", groupReceiver.getGroupID());
        chatlist.put("type", "group");
        chatlist.put("timeSend", System.currentTimeMillis());

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(mUser.getUid()).child(groupReceiver.getGroupID());
        databaseReference1.updateChildren(chatlist);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){

                    databaseReference1.updateChildren(chatlist);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupReceiver.getGroupID()).child("participant")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dn : snapshot.getChildren()){

                            HashMap<String, Object> chatlist1 = new HashMap<>();
                            chatlist1.put("id", groupReceiver.getGroupID());
                            chatlist1.put("type", "group");
                            chatlist1.put("timeSend", System.currentTimeMillis());

                            DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("ChatList")
                                    .child(dn.child("id").getValue()+"").child(groupReceiver.getGroupID());
                            databaseReference2.updateChildren(chatlist1);
                            databaseReference2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()){
                                        databaseReference2.updateChildren(chatlist1);

                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendImageMessage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String idVideo = UUID.randomUUID().toString();
        layoutProgress.setVisibility(View.VISIBLE);

        for (int i = 0; i < arrImageUri.size(); i++) {
            Uri uri = arrImageUri.get(i);
            StorageReference fileSaveURL =storageReference.child("image/"+uri.getLastPathSegment()+"_"+idVideo);
            fileSaveURL.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isSuccessful()) ;

                    String fileName = getFileName(uri, getApplicationContext());
                    String sizeFile = String.valueOf(taskSnapshot.getTotalByteCount());

                    Uri downloadURI = task.getResult();
                    GroupMessage message = new GroupMessage(mUser.getUid(), downloadURI.toString(), timeSend, messageType, fileName,sizeFile, false);
                    sendMessage(message);
                    arrImageUri.clear();
                    layoutDisplayMessageSelect.setVisibility(View.GONE);
                    layoutProgress.setVisibility(View.GONE);
                    layoutDisplayMessage.scrollToPosition(arrMessage.size() - 1);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress= 100 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                    loading.setProgress((int) progress);
                    loadingView.setText("Đang gửi: "+progress +"%");
                }
            });
        }
    }
    private void sendVideoMessage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String idVideo = UUID.randomUUID().toString();
        layoutProgress.setVisibility(View.VISIBLE);

        for (int i = 0; i < arrVideoUri.size(); i++) {
            Uri uri = arrVideoUri.get(i);
            StorageReference fileSaveURL =storageReference.child("video/"+uri.getLastPathSegment()+"_"+idVideo);
            fileSaveURL.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isSuccessful()) ;
                    String fileName = getFileName(uri, getApplicationContext());
                    String sizeFile = String.valueOf(taskSnapshot.getTotalByteCount());
                    Uri downloadURI = task.getResult();
                    GroupMessage message = new GroupMessage(mUser.getUid(), downloadURI.toString(), timeSend, messageType, fileName,sizeFile, false);
                    sendMessage(message);
                    arrVideoUri.clear();
                    layoutDisplayMessageSelect.setVisibility(View.GONE);
                    layoutProgress.setVisibility(View.GONE);
                    layoutDisplayMessage.scrollToPosition(arrMessage.size() - 1);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress= 100 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                    loading.setProgress((int) progress);
                    loadingView.setText("Đang gửi: "+progress +"%");
                }
            });
        }
    }
    private void sendFileMessage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String idFile = UUID.randomUUID().toString();
        layoutProgress.setVisibility(View.VISIBLE);

        for (int i = 0; i < arrFileUri.size(); i++) {
            Uri uri = arrFileUri.get(i);
            StorageReference fileSaveURL =storageReference.child("file/"+uri.getLastPathSegment()+"_"+idFile);
            fileSaveURL.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isSuccessful()) ;
                    String fileName = getFileName(uri, getApplicationContext());
                    String sizeFile = String.valueOf(taskSnapshot.getTotalByteCount());
                    Uri downloadURI = task.getResult();
                    GroupMessage message = new GroupMessage(mUser.getUid(), downloadURI.toString(), timeSend, messageType, fileName,sizeFile, false);
                    sendMessage(message);
                    arrFileUri.clear();
                    layoutDisplayMessageSelect.setVisibility(View.GONE);
                    layoutProgress.setVisibility(View.GONE);
                    layoutDisplayMessage.scrollToPosition(arrMessage.size() - 1);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress= 100 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                    loading.setProgress((int) progress);
                    loadingView.setText("Đang gửi: "+progress +"%");
                }
            });
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

    public void showPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.message_type);
        Object menuHelper;
        Class[] argTypes;
        try {
            @SuppressLint("DiscouragedPrivateApi") Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[]{boolean.class};
            assert menuHelper != null;
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception ignored) {

        }
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.send_image:
                selectMessage = "image";
                PermissionPickGallery();
                break;
            case R.id.send_video:
                selectMessage = "video";
                PermissionPickGallery();
                break;
            case R.id.send_file:
                selectMessage = "pdf";
                PermissionPickGallery();
                break;
        }
        return  true;
    }

    private void PermissionPickGallery(){
        if (ActivityCompat.checkSelfPermission(ChatGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_PICK_GALLERY);

        }else {
            if (selectMessage.equals("image")){
                pickImageGallery();
            }else if (selectMessage.equals("video")){
                pickVideoGallery();
            }else if (selectMessage.equals("pdf")){
                pickFilePDF();
            }
        }
    }

    private void PermissionCameraPicture(){
        if (ActivityCompat.checkSelfPermission(ChatGroupActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(ChatGroupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatGroupActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_CAMERA);
        }else {
            selectMessage = "image";
            pictureCamera();
        }
    }
    //Lấy ảnh từ thư viện
    private void pickImageGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_PICK_IMAGE_CODE);
    }
    //Lấy video từ thư viện
    private void pickVideoGallery(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_PICK_VIDEO_CODE);
    }
    //LẤy file PDF từ thư viện
    private void pickFilePDF(){
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_PICK_PDF_CODE);
    }

    //Lấy ảnh khi chụp
    private void pictureCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA_PICTURE_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){

            //Lấy ảnh thư viện
            if (requestCode == REQUEST_PICK_IMAGE_CODE){
                if (data.getClipData() != null){
                    //Lấy nhiều ảnh
                    int count = data.getClipData().getItemCount();
                    for (int i=0; i < count ; i++){
                        Uri uriImage =data.getClipData().getItemAt(i).getUri();
                        arrImageUri.add(uriImage);
                    }
                }else {
                    //LẤy một ảnh
                    Uri imageUri = data.getData();
                    arrImageUri.add(imageUri);
                }

                //Lấy video thư viện
            }else if(requestCode == REQUEST_PICK_VIDEO_CODE){
                if (data.getClipData() != null){
                    //Lấy nhiều video
                    int count = data.getClipData().getItemCount();
                    for (int i=0; i < count ; i++){
                        Uri uriVideo =data.getClipData().getItemAt(i).getUri();
                        arrVideoUri.add(uriVideo);
                    }
                }else {
                    //LẤy một video
                    Uri uriVideo = data.getData();
                    arrVideoUri.add(uriVideo);
                }

                //LẤy file từ thư viện
            }else if(requestCode == REQUEST_PICK_PDF_CODE){
                if (data.getClipData() != null){
                    //Lấy nhiều video
                    int count = data.getClipData().getItemCount();
                    for (int i=0; i < count ; i++){
                        Uri uriPDF =data.getClipData().getItemAt(i).getUri();
                        arrFileUri.add(uriPDF);
                    }
                }else {
                    //LẤy một video
                    Uri uriPDF = data.getData();
                    arrFileUri.add(uriPDF);
                }

                //Lấy ảnh từ camera
            }else if(requestCode == REQUEST_CAMERA_PICTURE_CODE){
                if (data != null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    Uri uriImage = convertURI(getApplicationContext(), bitmap);
                    arrImageUri.add(uriImage);

                }
            }

            if (arrImageUri.size()>0){
                layoutDisplayMessageSelect.setVisibility(View.VISIBLE);
                pickGallery = new LayoutMessagePickGallery(arrImageUri, selectMessage);
                layoutDisplayMessageSelect.setAdapter(pickGallery);

            }else if (arrVideoUri.size() >0){
                layoutDisplayMessageSelect.setVisibility(View.VISIBLE);
                pickGallery = new LayoutMessagePickGallery(arrVideoUri, selectMessage);
                layoutDisplayMessageSelect.setAdapter(pickGallery);

            }else if (arrFileUri.size() >0){
                layoutDisplayMessageSelect.setVisibility(View.VISIBLE);
                pickGallery = new LayoutMessagePickGallery(arrFileUri, selectMessage);
                layoutDisplayMessageSelect.setAdapter(pickGallery);
            }else {
                layoutDisplayMessageSelect.setVisibility(View.GONE);
            }
        }
    }

    private Uri convertURI(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String time = String.valueOf(System.currentTimeMillis());
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, time, null);
        return Uri.parse(path);
    }

    private void pushNotification(GroupMessage message, List<String> arrToken) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                databaseReference.child(groupReceiver.getGroupID()).child("Message").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dn : snapshot.getChildren()){
                            GroupMessage msg = dn.getValue(GroupMessage.class);
                            assert msg != null;
                            if (message.getTime().equals(msg.getTime())){
                                if (!msg.isSeen()){

                                ApiClient.pushNotificationGroupMSG(getApplicationContext(),arrToken, userSender,msg);
                                    Log.d("FCM", msg.toString());
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }, 2000);
    }

    private  void getTokenUserGroup(GroupMessage message){
        List<String>arrTokenn = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupReceiver.getGroupID()).child("participant")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dn : snapshot.getChildren()) {
                            arrUserID.add(dn.child("id").getValue().toString());
                        }

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    arrTokenn.clear();
                                    for (DataSnapshot dn : snapshot.getChildren()){
                                        Users users = dn.getValue(Users.class);

                                        assert users != null;
                                      for (String id : arrUserID){
                                          if (id.equals(users.getId())){
                                              arrTokenn.add(users.getToken());
                                          }
                                      }

                                        pushNotification(message, arrTokenn);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    //Check user online
    private void CheckUserChatOnline(String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        databaseReference.updateChildren(map);
    }


    @Override
    protected void onResume() {
        super.onResume();
        CheckUserChatOnline("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        String time = String.valueOf(System.currentTimeMillis());
        CheckUserChatOnline(time);
        seenRef.removeEventListener(seenListener);
    }
}