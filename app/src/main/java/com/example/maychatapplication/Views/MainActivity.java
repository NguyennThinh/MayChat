package com.example.maychatapplication.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maychatapplication.Adapter.MainViewPagerAdapter;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements  PopupMenu.OnMenuItemClickListener{
    private BottomNavigationView bottom_navigation;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private CircleImageView imgUser;
    private TextView title;
    FirebaseUser mUser ;

    private Users users;
    private PreferenceManager preferenceManager;



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        innitUI();

        if (!users.getAvatar().equals("default")){
            Picasso.get().load(users.getAvatar()).into(imgUser);
        }

        imgUser.setOnClickListener(this::showPopup);

        setUpViewPager();
        bottomNavigationClick();
    }



    private void innitUI() {
        viewPager = findViewById(R.id.layout_fragment);
        bottom_navigation = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
        imgUser = findViewById(R.id.user_avatar);
        title = findViewById(R.id.title);

        preferenceManager = new PreferenceManager(getApplicationContext());
        String value = preferenceManager.getString("users");
        Gson gson = new Gson();
        users = gson.fromJson(value, Users.class);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(toolbar);
    }

    private void setUpViewPager() {
        MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position){
                    case 0:
                        title.setText("Tin nhắn");
                        getSupportActionBar().setTitle("");
                        bottom_navigation.getMenu().findItem(R.id.ListChat).setChecked(true);
                        break;
                    case 1:
                        title.setText("Nhân viên công ty");
                        getSupportActionBar().setTitle("");
                        bottom_navigation.getMenu().findItem(R.id.employee).setChecked(true);
                        break;
                    case 2:
                        title.setText("Nhóm công việc");
                        getSupportActionBar().setTitle("");
                        bottom_navigation.getMenu().findItem(R.id.Group).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    private void bottomNavigationClick() {
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ListChat:
                        title.setText("Tin nhắn");
                        getSupportActionBar().setTitle("");
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.employee:
                        title.setText("Nhân viên");
                        getSupportActionBar().setTitle("");
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.Group:
                        title.setText("Nhóm");
                        getSupportActionBar().setTitle("");
                        viewPager.setCurrentItem(2);
                        break;
                }
                return true;
            }

        });
    }

    public void showPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.menu_personal);

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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                preferenceManager.clearPreference();
                FirebaseAuth mUAuth = FirebaseAuth.getInstance();
                mUAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
            case R.id.personal:
                startActivity(new Intent(this, PersonalActivity.class));
                break;
        }
        return  true;
    }

    //Check user online
    private void CheckUserChatOnline(String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        databaseReference.updateChildren(map);
    }

    @Override
    protected void onStart() {
        CheckUserChatOnline("online");
        super.onStart();
    }

    @Override
    protected void onResume() {

        super.onResume();

        CheckUserChatOnline("online");


    }

    @Override
    protected void onPause() {

        String time = String.valueOf(System.currentTimeMillis());
        CheckUserChatOnline(time);
        super.onPause();

    }



}