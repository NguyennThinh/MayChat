package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.maychatapplication.Interface.iAddMemberGroup;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CreateWorkActivity extends AppCompatActivity {
    //Views
    private EditText workName, userCreateName, workDescription, workStart, workEnd, userInChange;
    ImageButton imgUserChange;
    private Button btnCreateWork;

    PreferenceManager preferenceManager;

    private Users users;

    //Declare Firebase
    private FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_work);

        initUI();


        workStart.setOnClickListener(view -> {
            selectDateStart();
        });
        workEnd.setOnClickListener(view->{
            selectDateEnd();
        });

        imgUserChange.setOnClickListener(view->{
            Intent i = new Intent(this, LeaderWorkActivity.class);
            startActivityForResult(i, 1);
        });

        btnCreateWork.setOnClickListener(view->{
            createWork();
        });
    }




    private void initUI() {
        workName = findViewById(R.id.work_name);
        userCreateName = findViewById(R.id.user_create);
        workDescription = findViewById(R.id.work_description);
        workStart = findViewById(R.id.work_start);
        workEnd = findViewById(R.id.work_end);
        userInChange = findViewById(R.id.user_in_change);
        imgUserChange = findViewById(R.id.img_user_change);
        btnCreateWork = findViewById(R.id.btn_create_work);

        mUser = FirebaseAuth.getInstance().getCurrentUser();


        preferenceManager = new PreferenceManager(this);
        Gson gson = new Gson();
        String myUser = preferenceManager.getString("users");
        users = gson.fromJson(myUser, Users.class);

        userCreateName.setText(users.getFullName());

    }

    private void selectDateStart() {

        Calendar date = Calendar.getInstance();
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);


        DatePickerDialog pickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                workStart.setText(dateFormat.format(date.getTime()));
            }
        },year, month, day);
        pickerDialog.show();
    }

    private void selectDateEnd() {
        Calendar date = Calendar.getInstance();
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);


        DatePickerDialog pickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                workEnd.setText(dateFormat.format(date.getTime()));
            }
        },year, month, day);
        pickerDialog.show();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                users = (Users) data.getExtras().get("leader");
               userInChange.setText(users.getFullName());
            }
        }
    }
    private void createWork() {
        String work_name = workName.getText().toString().trim();
        String userCreate = userCreateName.getText().toString().trim();
        String description = workDescription.getText().toString().trim();
        String work_start = workStart.getText().toString().trim();
        String work_end = workEnd.getText().toString().trim();
        String user_change = userInChange.getText().toString().trim();

        HashMap<String, Object> work = new HashMap<>();
        work.put("workName", work_name);
        work.put("userCreate",mUser.getUid());
        work.put("workDescription", description);
        work.put("workStart", work_start);
        work.put("workEnd", work_end);
        work.put("leader", users.getId());
        work.put("workCreateDate", System.currentTimeMillis());
        work.put("workStatus", 1);
        work.put("dateComplete", "");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("WorkList");
        databaseReference.child(mUser.getUid()).setValue(work);
    }
}