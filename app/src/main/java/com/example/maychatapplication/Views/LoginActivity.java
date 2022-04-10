package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity {
    //Views
    private TextInputEditText inputEmail, inputPass;
    private Button btnLogin;
    private ProgressBar progressBarLogin;

    //Declare Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();

        TextView views = findViewById(R.id.textView2);
        preferenceManager = new PreferenceManager(getApplicationContext());


        views.setOnClickListener(view->{
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(view -> {
            userLogin();
        });
    }

    private void initUI() {
        inputEmail = findViewById(R.id.inputEmail);
        inputPass = findViewById(R.id.inputPass);

        btnLogin = findViewById(R.id.btnLogin);
        progressBarLogin = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();


    }
    private void userLogin() {
        String email = inputEmail.getText().toString().trim();
        String pass = inputPass.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Email không đúng định dạng");
            inputEmail.setFocusable(true);
        } else if (pass.length() < 6) {
            inputPass.setError("Mật khẩu phải lớn hơn 6 ký tự");
            inputPass.setFocusable(true);
        } else {
            btnLogin.setVisibility(View.INVISIBLE);
            progressBarLogin.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tasks -> {
                                if (tasks.isSuccessful()) {

                                    DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference("Users");
                                    databaseReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                Users users = snapshot.getValue(Users.class);

                                                Gson gson = new Gson();
                                                String myUser = gson.toJson(users);
                                                preferenceManager.putString("users", myUser);
                                                String token = tasks.getResult();

                                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                                databaseReference.child(mUser.getUid()).child("token").setValue(token);
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }

                            });
                        }else {
                            btnLogin.setVisibility(View.VISIBLE);
                            progressBarLogin.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Đăng nhập không thành công", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}