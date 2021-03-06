package com.tech.petabyteboy.hisaab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tech.petabyteboy.hisaab.Models.HomeDuesModel;
import com.tech.petabyteboy.hisaab.Models.UserModel;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, View.OnClickListener {

    private DrawerLayout drawerLayout;
    private int selectedItem;

    private SimpleDraweeView imgProfile;
    private SimpleDraweeView navProfile;
    private TextView txtUserName;

    private TextView navUsername;
    private TextView navEmailID;

    private FirebaseAuth auth;
    private DatabaseReference userdataReference;

    public static TextView txtAmount;

    public static UserModel User;
    private HomeDuesModel homeDuesModel;

    private String TAG = "MainActivity";

    public static int REQUEST_CODE_PROFILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FragmentManager FragmentManager = getSupportFragmentManager();
        FragmentTransaction FragmentTransaction = FragmentManager.beginTransaction();
        FragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navDrawer = (NavigationView) findViewById(R.id.menu_drawer);
        navDrawer.setNavigationItemSelectedListener(this);

        View navDrawerHeaderView = navDrawer.getHeaderView(0);

        ImageButton btn_edit_profile = (ImageButton) navDrawerHeaderView.findViewById(R.id.btn_edit_profile);
        btn_edit_profile.setOnClickListener(this);

        navUsername = (TextView) navDrawerHeaderView.findViewById(R.id.username);
        navEmailID = (TextView) navDrawerHeaderView.findViewById(R.id.user_mail);

        navProfile = (SimpleDraweeView) navDrawerHeaderView.findViewById(R.id.imgUsrProfile);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.app_name, R.string.app_name);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        selectedItem = savedInstanceState == null ? R.id.home : savedInstanceState.getInt("selectedItem");

        imgProfile = (SimpleDraweeView) findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(this);

        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtUserName.setOnClickListener(this);

        txtAmount = (TextView) findViewById(R.id.txtAmount);

        SharedPreferences userPref = getSharedPreferences(RegisterActivity.PREF_NAME, MODE_PRIVATE);
        Log.e(TAG, "Phone No : " + userPref.getString("phone", null));
        String UserID = userPref.getString("phone", null);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        auth = FirebaseAuth.getInstance();

        userdataReference = firebaseDatabase.getReference().child("Users").child(UserID);

        userdataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User = dataSnapshot.getValue(UserModel.class);
                setValues();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.e(TAG,"User ID : "+UserID);
        final DatabaseReference homedueDataRef = firebaseDatabase.getReference().child("HomeDues").child(UserID);

        homedueDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    homeDuesModel = dataSnapshot.getValue(HomeDuesModel.class);
                    if (homeDuesModel.getAmount().isEmpty()) {
                        txtAmount.setText("Your Get : \u20B9 0.00");
                    } else {
                        if (homeDuesModel.getPayType().equalsIgnoreCase("iPay")) {
                            txtAmount.setText("You Pay : \u20B9 " + homeDuesModel.getAmount().replace("-",""));
                        } else if (homeDuesModel.getPayType().equalsIgnoreCase("NoDue")) {
                            txtAmount.setText("Your Get : \u20B9 0.00");
                        } else if (homeDuesModel.getPayType().equalsIgnoreCase("iGet")) {
                            txtAmount.setText("You Get : \u20B9 " + homeDuesModel.getAmount().replace("-",""));
                        }
                    }
                }
                else {
                    txtAmount.setText("Your Get : \u20B9 0.00");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setValues() {

        navUsername.setText(User.getUsername());
        txtUserName.setText(User.getUsername());

        navEmailID.setText(User.getEmailID());

        if (User.getImage().isEmpty() || User.getImage().equalsIgnoreCase("")) {
            Uri uri = new Uri.Builder().scheme("res") // "res"
                    .path(String.valueOf(R.drawable.profile_pic_home)).build();
            imgProfile.setImageURI(uri);
            navProfile.setImageURI(uri);
        } else {
            imgProfile.setImageURI(User.getImage());
            navProfile.setImageURI(User.getImage());
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        item.setChecked(true);
        selectedItem = item.getItemId();

        switch (selectedItem) {
            case R.id.home:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.invite_friends:
                Toast.makeText(this, "Invite Friends is clicked !", Toast.LENGTH_SHORT).show();
                break;
            case R.id.recent_activity:
                startActivity(new Intent(this, RecentActivities.class));
                break;
            case R.id.favorites:
                Toast.makeText(this, "Favorite is clicked !", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "Settings is clicked !", Toast.LENGTH_SHORT).show();
                break;

            case R.id.signout:
                auth.signOut();
                startActivity(new Intent(this, SplashScreenActivity.class));
                finish();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.search:

                break;

            case R.id.history:
                Intent intent = new Intent(this, RecentActivities.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putInt("selectedItem", selectedItem);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgProfile:
            case R.id.txtUserName:

                Intent intent = new Intent(this, ProfileActivity.class);
                startActivityForResult(intent,REQUEST_CODE_PROFILE);
                break;

            case R.id.btn_edit_profile:
                startActivityForResult(new Intent(getApplication(), ProfileActivity.class),REQUEST_CODE_PROFILE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PROFILE && resultCode == RESULT_OK){
            userdataReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User = dataSnapshot.getValue(UserModel.class);
                    setValues();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
