package com.alimustofa.kloset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //Firebase auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    //view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //default fragment transaction
        actionBar.setTitle("Home");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 =getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        //bottom navigation
        BottomNavigationView navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            //handle item click
            switch ((menuItem.getItemId())){
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                    HomeFragment fragment1 = new HomeFragment();
                    FragmentTransaction ft1 =getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content, fragment1, "");
                    ft1.commit();
                return true;

                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    UsersFragment fragment2 = new UsersFragment();
                    FragmentTransaction ft2 =getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content, fragment2, "");
                    ft2.commit();
                    return true;

                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment fragment3 = new ProfileFragment();
                    FragmentTransaction ft3 =getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content, fragment3, "");
                    ft3.commit();
                    return true;
            }
            return false;
        }
    };

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null){
            //user sign and stay here

            //set email of loggged in user

        }else{
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    //inflate action menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //indlating menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handle menu item

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}
