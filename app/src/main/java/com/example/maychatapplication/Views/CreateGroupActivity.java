package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maychatapplication.Adapter.LayoutUserSelectAdapter;
import com.example.maychatapplication.Adapter.LayoutUserSelectGroupAdapter;
import com.example.maychatapplication.Interface.iAddMemberGroup;
import com.example.maychatapplication.Model.Participant;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity implements iAddMemberGroup, PopupMenu.OnMenuItemClickListener {
    private static final int REQUEST_CAMERA_PICTURE_CODE = 100;
    private static final int REQUEST_PERMISSION_CODE_CAMERA = 101;
    private static final int REQUEST_PERMISSION_CODE_PICK_GALLERY = 102;
    private static final int REQUEST_PICK_IMAGE_CODE =103 ;
    //Views
    private RecyclerView list_user_select, list_user_select_group;
    private LayoutUserSelectAdapter adapterUserSelect;
    private LayoutUserSelectGroupAdapter adapterUserSelectGroup;
    private LinearLayout layout_participant;
    private EditText inputGroupName;
    private ImageView imgCreateGroup,imgCamera, imgBack;

    //Declare list
    private List<Users> arrUsers, arrUserSelect;
    private List<Participant> userGroups ;

    //Declare Firebase
    private FirebaseUser mUser;
    private StorageReference  storageReference;


    private Uri imgUri;
    private String selectImage;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initUI();


        loadEmployee();

        imgBack.setOnClickListener(view->{
            finish();
        });

        imgCamera.setOnClickListener(view->{
           showPopup(view);
        });

        imgCreateGroup.setOnClickListener(view -> {
            createGroup(imgUri);
        });
    }

    private void initUI() {
        list_user_select = findViewById(R.id.display_user);
        list_user_select_group = findViewById(R.id.display_user_select_group);
        layout_participant = findViewById(R.id.layout_bottom);
        imgCamera = findViewById(R.id.img_group_avatar);
        inputGroupName = findViewById(R.id.input_group_name);
        imgCreateGroup = findViewById(R.id.create_group);
        imgBack = findViewById(R.id.img_back);

        arrUsers =  new ArrayList<>();
        userGroups =  new ArrayList<>();

        progressDialog = new ProgressDialog(this);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();


        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        list_user_select.setLayoutManager(layoutManager);
        list_user_select.addItemDecoration(itemDecoration);
    }

    private void loadEmployee() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrUsers.clear();
                for (DataSnapshot dn : snapshot.getChildren()){
                    Users users = dn.getValue(Users.class);

                    if (users != null ){
                        if (!users.getId().equals(mUser.getUid())){
                            arrUsers.add(users);
                        }

                    }
                }

                adapterUserSelect = new LayoutUserSelectAdapter(arrUsers, CreateGroupActivity.this);
                list_user_select.setAdapter(adapterUserSelect);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void addMemberGroup(List<Users> arrEmployees) {
        arrUserSelect = arrEmployees;

        if (arrUserSelect.size() >0){
            layout_participant.setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
            list_user_select_group.setLayoutManager(layoutManager);
            adapterUserSelectGroup = new LayoutUserSelectGroupAdapter(arrUserSelect);
            list_user_select_group.setAdapter(adapterUserSelectGroup);

        }else {
            layout_participant.setVisibility(View.GONE);
        }

    }

    @Override
    public void addLeader(Users users) {

    }


    private void RequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_CAMERA);


        }else  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_PICK_GALLERY);

        } else {
                if (selectImage.equals("gallery")){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, REQUEST_PICK_IMAGE_CODE);
                }else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA_PICTURE_CODE);
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if ( requestCode == REQUEST_CAMERA_PICTURE_CODE){
                if (data != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        imgUri = convertURI(this, bitmap);
                        imgCamera.setImageBitmap(bitmap);

                }
            }else if (requestCode == REQUEST_PICK_IMAGE_CODE){
                    Uri imageUri = data.getData();
                    imgCamera.setImageURI(imageUri);
                    imgUri = imageUri;
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





    private void createGroup(Uri uri) {
        progressDialog.setMessage("Đang tạo nhóm...");
        progressDialog.show();
        String random = UUID.randomUUID().toString();

        StorageReference imageURL = storageReference.child("image/"+random + "_" + mUser.getUid());
        imageURL.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();

                while (!task.isSuccessful()) ;
                Uri downloadURI = task.getResult();
                if (task.isSuccessful()) {

                    String timeCreate = String.valueOf(System.currentTimeMillis());
                    String groupID = UUID.randomUUID().toString();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("groupID", groupID);
                    map.put("groupName", inputGroupName.getText().toString().trim());
                    map.put("userCreate", mUser.getUid());
                    map.put("description", "");
                    map.put("groupImage", downloadURI.toString());
                    map.put("createDate", timeCreate);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                    databaseReference.child(groupID).setValue(map);

                    userGroups.add(new Participant(mUser.getUid(), 1, timeCreate));
                    for (Users users: arrUserSelect){
                        userGroups.add(new Participant(users.getId(),3, timeCreate));

                    }
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Groups")
                            .child(groupID);
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (Participant p : userGroups){
                                databaseReference1.child("participant").child(p.getId()).setValue(p);
                            }
                            progressDialog.dismiss();
                            startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                        }
                    });



                }

            }
        });
    }











    public void showPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.select_image);
        popupMenu.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.gallery:
                selectImage = "gallery";
                RequestPermission();
                break;
            case R.id.camera:
                selectImage = "camera";
                RequestPermission();
                break;

        }
        return  true;
    }
}