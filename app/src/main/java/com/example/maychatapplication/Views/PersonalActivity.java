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
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_IMAGE_CODE = 100;
    private static final int REQUEST_CAMERA_PICTURE_CODE =101 ;
    private static final int REQUEST_PERMISSION_CODE_PICK_GALLERY = 102;
    private static final int REQUEST_PERMISSION_CODE_CAMERA =103 ;
    //Views
    private ImageView imgBackfround;
    private CircleImageView imgUser;
    private TextView tvName, tvBirthday, tvGender, tvPhone, tvEmail;
    private FloatingActionButton btnEdit;
    private ProgressDialog progressDialog;

    //Declare Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    PreferenceManager preferenceManager;

    Users users;

    String editOption;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        //Initialize views
        initUI();

        loadProfile();

        btnEdit.setOnClickListener(view->{
            chooseEditProfile();
        });
    }

    private void initUI() {
        imgBackfround  = findViewById(R.id.imgBackground);
        imgUser = findViewById(R.id.imgUser);
        tvName = findViewById(R.id.tv_Name);
        tvBirthday = findViewById(R.id.tv_birthday);
        tvGender = findViewById(R.id.tv_gender);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        btnEdit = findViewById(R.id.btnEditProfile);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(this);

        preferenceManager = new PreferenceManager(this);

        Gson gson = new Gson();
        String u = preferenceManager.getString("users");

        users = gson.fromJson(u, Users.class);
    }

    private void loadProfile() {
        Toast.makeText(getApplicationContext(), users.getAvatar()+"", Toast.LENGTH_SHORT).show();
        if (users != null){
            if (users.getAvatar().equals("default")){
                imgUser.setImageResource(R.drawable.ic_launcher_background);
            }else{
                Picasso.get().load(users.getAvatar()).into(imgUser);
            }

            if (users.getBackgroundPhoto().equals("default")) {
                imgBackfround.setImageResource(R.drawable.ic_launcher_background);
            } else {
                Picasso.get().load(users.getBackgroundPhoto()).into(imgBackfround);
            }
            tvName.setText(users.getFullName());
            tvBirthday.setText(users.getBirthday());
            tvPhone.setText(users.getPhone());
            tvEmail.setText(users.getEmail());
            tvGender.setText(users.getGender());
        }
    }
    private void chooseEditProfile() {
        //Option edit
        String[] option = {"Cập nhật ảnh đại diện", "Cập nhật tên","Cập nhật ngày sinh", "Cập nhật số điện thoại", "Cập nhật ảnh bìa"};

        //Dialog edit
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật");
        //set Item for dialog
        builder.setItems(option, (dialogInterface, i) -> {
            switch (i){
                case 0:
                    editOption="avatar";
                    ChooseOptionEditImage();

                    break;
                case 1:
                    EditNameDialog();
                    break;
                case 2:
                    EditBirthDialog();
                    break;
                case 3:
                    EditPhoneDialog();

                    break;
                case 4:
                    editOption = "background";
                    ChooseOptionEditImage();

                    break;
            }
        });
        builder.create().show();
    }
    private void EditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật tên");
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
                map.put("fullName", value);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(mUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
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
    private void EditBirthDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật ngày sinh");
        //setLayout Dialog
        LinearLayout layout = new LinearLayout(this);

        //set views in dialog
        EditText edtKey = new EditText(this);
        edtKey.setMinEms(15);
        edtKey.setInputType( InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_DATE);
        layout.addView(edtKey);
        layout.setPadding(10,10,10,0);

        builder.setView(layout);
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = edtKey.getText().toString().trim();
                HashMap<String, Object> map = new HashMap<>();
                map.put("birthday", value);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(mUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
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
    private void EditPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật số điện thoại");
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
                map.put("phone", value);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(mUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
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
        if (ActivityCompat.checkSelfPermission(PersonalActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PersonalActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_PICK_GALLERY);

        }else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICK_IMAGE_CODE);
        }
    }
    private void PermissionCameraPicture(){
        if (ActivityCompat.checkSelfPermission(PersonalActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(PersonalActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PersonalActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_CAMERA);
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
                if (editOption.equals("avatar")){

                    HashMap<String , Object> map = new HashMap<>();
                    map.put("avatar", downloadURI.toString());
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                    databaseReference.child(mUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            imgUser.setImageURI(uri);
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {

                    HashMap<String , Object> map = new HashMap<>();
                    map.put("backgroundPhoto", downloadURI.toString());
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                    databaseReference.child(mUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            imgBackfround.setImageURI(uri);
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

    }
}