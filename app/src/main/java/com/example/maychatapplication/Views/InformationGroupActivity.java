package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maychatapplication.Model.Group;
import com.example.maychatapplication.Model.GroupMessage;
import com.example.maychatapplication.Model.Participant;
import com.example.maychatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class InformationGroupActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_IMAGE_CODE = 100;
    private static final int REQUEST_CAMERA_PICTURE_CODE =101 ;
    private static final int REQUEST_PERMISSION_CODE_CAMERA = 102;
    private static final int REQUEST_PERMISSION_CODE_PICK_GALLERY = 103;
    //Views
    private ImageView imgBack, imgEdit;
    private CircleImageView imgGroup;
    private TextView groupName, groupDescription;
    private TextView listParticipant, addParticipant,deleteGroup, leaveGroup;
    private Group group;

    private ProgressDialog progressDialog;

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_group);

        initUI();
        loadInfoGroup();


        imgBack.setOnClickListener(view->{
            finish();
        });
        imgEdit.setOnClickListener(view->{
            ChooseOptionEdit();
        });
        listParticipant.setOnClickListener(view->{
            Intent intent = new Intent(this, ListParticipantActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("groups", group);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        addParticipant.setOnClickListener(view->{
            Intent intent = new Intent(this, AddParticipantActivity.class);

            intent.putExtra("group_id", group.getGroupID());
            startActivity(intent);
        });
        deleteGroup.setOnClickListener(view->{




            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
            databaseReference.child(group.getGroupID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Group group = snapshot.getValue(Group.class);
                    if (group.getUserCreate().equals(mUser.getUid())){
                       deleteGroupp();
                    }else {
                        Toast.makeText(getApplicationContext(), "Bạn khong có quyền xóa nhóm", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            
        });
    }



    private void initUI() {
        imgBack = findViewById(R.id.img_back);
        imgEdit = findViewById(R.id.img_edit_info_group);
        imgGroup = findViewById(R.id.img_group_avatar);
        groupName = findViewById(R.id.group_name);
        groupDescription = findViewById(R.id.group_description);
        listParticipant = findViewById(R.id.list_participant);
        addParticipant = findViewById(R.id.add_participant);
        deleteGroup = findViewById(R.id.delete_group);
        leaveGroup = findViewById(R.id.leave_group);

        group = (Group) getIntent().getExtras().get("my_group");

        progressDialog = new ProgressDialog(this);
    }

    private void loadInfoGroup() {
        Picasso.get().load(group.getGroupImage()).into(imgGroup);
        groupName.setText(group.getGroupName());
        if (group.getDescription().equals("default")){
            groupDescription.setVisibility(View.GONE);
        }else {
            groupDescription.setText(group.getDescription());
        }

    }


    private void ChooseOptionEdit() {
        String[] option = {"Cập nhật ảnh nhóm", "Cập nhật tên nhóm", "CẬp nhật giới thiệu nhóm"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(option, (dialogInterface, i) -> {
            switch (i){
                case 0:
                    ChooseOptionEditImage();

                    break;
                case 1:
                    EditNameDialog();
                    break;
                case 2:
                    EditDescriptionDialog();
                    break;
            }
        });

        builder.create().show();
    }

    private void EditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật tên nhóm");
        //setLayout Dialog
        LinearLayout layout = new LinearLayout(this);

        //set views in dialog
        EditText edtKey = new EditText(this);
        edtKey.setMinEms(15);

        layout.addView(edtKey);
        layout.setPadding(10,10,10,0);

        builder.setView(layout);
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = edtKey.getText().toString().trim();
                HashMap<String, Object> map = new HashMap<>();
                map.put("groupName", value);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                databaseReference.child(group.getGroupID()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Câp nhật thành công", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //button cancel
        builder.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //disable dialog
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
    private void EditDescriptionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật giới thiệu");
        //setLayout Dialog
        LinearLayout layout = new LinearLayout(this);

        //set views in dialog
        EditText edtKey = new EditText(this);
        edtKey.setMinEms(15);

        layout.addView(edtKey);
        layout.setPadding(10,10,10,0);

        builder.setView(layout);
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = edtKey.getText().toString().trim();
                HashMap<String, Object> map = new HashMap<>();
                map.put("description", value);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                databaseReference.child(group.getGroupID()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Câp nhật thành công", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //button cancel
        builder.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //disable dialog
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void ChooseOptionEditImage() {
        String[] option = {"Chọn từ thư viện", "Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(option, (dialogInterface, i) -> {
            switch (i){
                case 0:
                    PermissionPickGallery();

                    break;
                case 1:
                    PermissionCameraPicture();
                    break;

            }
        });

        builder.create().show();
    }

    private void PermissionPickGallery(){
        if (ActivityCompat.checkSelfPermission(InformationGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InformationGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_PICK_GALLERY);

        }else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICK_IMAGE_CODE);
        }
    }
    private void PermissionCameraPicture(){
        if (ActivityCompat.checkSelfPermission(InformationGroupActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(InformationGroupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InformationGroupActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_CAMERA);
        }else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA_PICTURE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_PICK_IMAGE_CODE){
                if(data != null){
                    Uri imageUri = data.getData();
                    sendImageMessage(imageUri);
                }
            }else if (requestCode == REQUEST_CAMERA_PICTURE_CODE){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                Uri uriImage = convertURI(getApplicationContext(), bitmap);
                sendImageMessage(uriImage);
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
    private void sendImageMessage(Uri uri) {
        progressDialog.setMessage("Đang cập nhật..");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String idVideo = UUID.randomUUID().toString();


            StorageReference fileSaveURL =storageReference.child("image/"+uri.getLastPathSegment()+"_"+idVideo);
            fileSaveURL.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isSuccessful()) ;

                    Uri downloadURI = task.getResult();

                    HashMap<String , Object> map = new HashMap<>();
                    map.put("groupImage", downloadURI.toString());
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                    databaseReference.child(group.getGroupID()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            imgGroup.setImageURI(uri);
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

    }

    private void deleteGroupp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa nhóm");
        builder.setMessage("Bạn muốn xóa nhóm");

        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ChatList");
                dbRef.child(mUser.getUid()).child(group.getGroupID()).removeValue();
                DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(group.getGroupID()).child("participant").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dn: snapshot.getChildren()){
                            Participant participant = dn.getValue(Participant.class);
                            if (!participant.getId().equals(mUser.getUid())){
                                dbRef.child(participant.getId()).child(group.getGroupID()).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                reference.child(group.getGroupID()).removeValue();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
        builder.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}