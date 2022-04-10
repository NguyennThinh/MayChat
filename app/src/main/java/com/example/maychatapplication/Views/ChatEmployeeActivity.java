package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
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
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.maychatapplication.Adapter.LayoutMessagePickGallery;
import com.example.maychatapplication.Adapter.LayoutMessageUserAdapter;
import com.example.maychatapplication.Fcm.ApiClient;
import com.example.maychatapplication.Model.SingleMessage;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.google.android.gms.dynamic.IFragmentWrapper;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatEmployeeActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    //Views
    private ImageView imgSelectMessage, imgAudioCall, imgBackActivity, imgCamera, imgSend;
    private EditText inputMessage;
    private RecyclerView layoutDisplayMessage, layoutDisplayMessageSelect;
    private CircleImageView imgUser;
    private TextView userName, userStatus;
    //adapter
    LayoutMessageUserAdapter adapter;
    LayoutMessagePickGallery pickGallery;
    //Declare Firebase
    private FirebaseUser mUser;
    private DatabaseReference seenRef;
    private ValueEventListener seenListener;

    //Declare user receiver;
    private Users userReceiver, userSender;

    private List<SingleMessage> arrMessage;

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
        setContentView(R.layout.activity_chat_employee);

        initUI();
        loadInfoUser();
        displayMessage();
        seenMessage(userReceiver.getId());

        imgSelectMessage.setOnClickListener(view -> {
            showPopup(view);
        });
        imgCamera.setOnClickListener(view -> {
            PermissionCameraPicture();
        });

        imgBackActivity.setOnClickListener(view -> {
            finish();
        });
        imgSend.setOnClickListener(view -> {
            String msg = inputMessage.getText().toString();


            if (!msg.equals("")) {
                messageType = "text";
                SingleMessage message = new SingleMessage(mUser.getUid(), userReceiver.getId(), msg, timeSend, messageType,"default","default", false);
                sendMessage(message);
                layoutDisplayMessage.scrollToPosition(arrMessage.size() - 1);
                pushNotification(message);

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
        imgAudioCall = findViewById(R.id.audio_call_user);
        imgBackActivity = findViewById(R.id.img_back);
        imgSelectMessage = findViewById(R.id.select_message_type);
        imgSend = findViewById(R.id.send_message);
        imgCamera = findViewById(R.id.select_camera);
        inputMessage = findViewById(R.id.input_message);
        imgUser = findViewById(R.id.img_user_receiver);
        userName = findViewById(R.id.tv_name_user_receiver);
        userStatus = findViewById(R.id.tv_user_receiver_status);

        layoutDisplayMessage = findViewById(R.id.layout_display_message);
        layoutDisplayMessageSelect = findViewById(R.id.layout_message_select);

        arrImageUri = new ArrayList<>();
        arrVideoUri = new ArrayList<>();
        arrFileUri = new ArrayList<>();
        arrMessage = new ArrayList<>();

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        userReceiver = (Users) getIntent().getExtras().get("users");

        PreferenceManager preferenceManager = new PreferenceManager(this);
        String user = preferenceManager.getString("users");
        Gson gson = new Gson();
        userSender = gson.fromJson(user, Users.class);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutDisplayMessage.setLayoutManager(layoutManager);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        layoutDisplayMessageSelect.setLayoutManager(linearLayoutManager);
    }

    private void loadInfoUser() {
        if (userReceiver.getAvatar().equals("default")){
            imgUser.setImageResource(R.drawable.ic_launcher_background);
        }else {
            Picasso.get().load(userReceiver.getAvatar()).into(imgUser);
        }
        userName.setText(userReceiver.getFullName());
        if (userReceiver.getStatus().equals("online")){
            userStatus.setText("Đang hoạt động");
            userStatus.setTextColor(Color.GREEN);
        }else {
            userStatus.setText("Không hoạt động");
            userStatus.setTextColor(Color.RED);
        }
    }


    private void PermissionPickGallery(){
        if (ActivityCompat.checkSelfPermission(ChatEmployeeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatEmployeeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_PICK_GALLERY);

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
        if (ActivityCompat.checkSelfPermission(ChatEmployeeActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(ChatEmployeeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatEmployeeActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_CAMERA);
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


    private void displayMessage() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Message");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Initialize list message
                arrMessage = new ArrayList<>();

                for (DataSnapshot dn : snapshot.getChildren()) {
                    SingleMessage message = dn.getValue(SingleMessage.class);

                    if (dn.child("idSender").getValue().equals(mUser.getUid()) && dn.child("idReceiver").getValue().equals(userReceiver.getId())
                            || dn.child("idSender").getValue().equals(userReceiver.getId()) && dn.child("idReceiver").getValue().equals(mUser.getUid())) {
                        arrMessage.add(message);

                    }

                }

                adapter = new LayoutMessageUserAdapter(arrMessage, userReceiver.getAvatar());
                layoutDisplayMessage.setAdapter(adapter);
                layoutDisplayMessage.scrollToPosition(arrMessage.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage() + "", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void seenMessage(String id) {
        seenRef = FirebaseDatabase.getInstance().getReference("Message");
        seenListener = seenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dn : snapshot.getChildren()) {
                    SingleMessage message = dn.getValue(SingleMessage.class);
                    if (message.getIdReceiver().equals(mUser.getUid()) && message.getIdSender().equals(id)) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("seen", true);
                        dn.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sendMessage(SingleMessage message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Message");
        databaseReference.push().setValue(message);


        //Views list user chat
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(mUser.getUid()).child(userReceiver.getId());
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {

                    databaseReference1.child("id").setValue(userReceiver.getId());
                    databaseReference1.child("type").setValue("single");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(userReceiver.getId()).child(mUser.getUid());
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    databaseReference2.child("id").setValue(mUser.getUid());
                    databaseReference2.child("type").setValue("single");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendFileMessage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String idFile = UUID.randomUUID().toString();
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
                    SingleMessage message = new SingleMessage(mUser.getUid(), userReceiver.getId(), downloadURI.toString(), timeSend, messageType, fileName,sizeFile, false);
                    sendMessage(message);
                    arrFileUri.clear();
                    layoutDisplayMessageSelect.setVisibility(View.GONE);
                }
            });
        }
    }

    private void sendVideoMessage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String idVideo = UUID.randomUUID().toString();
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
                    SingleMessage message = new SingleMessage(mUser.getUid(), userReceiver.getId(), downloadURI.toString(), timeSend, messageType, fileName,sizeFile, false);
                    sendMessage(message);
                    arrVideoUri.clear();
                    layoutDisplayMessageSelect.setVisibility(View.GONE);
                }
            });
        }
    }

    private void sendImageMessage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String idVideo = UUID.randomUUID().toString();
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
                    SingleMessage message = new SingleMessage(mUser.getUid(), userReceiver.getId(), downloadURI.toString(), timeSend, messageType, fileName,sizeFile, false);
                    sendMessage(message);
                    arrImageUri.clear();
                    layoutDisplayMessageSelect.setVisibility(View.GONE);
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






    private void pushNotification(SingleMessage message) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Message");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dn : snapshot.getChildren()){
                            SingleMessage msg = dn.getValue(SingleMessage.class);
                            assert msg != null;
                            if (message.getTime().equals(msg.getTime())){
                                if (!msg.isSeen()){
                                    ApiClient.pushNotificationSingleMSG(getApplicationContext(),userReceiver.getToken(), userSender,msg);
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
}