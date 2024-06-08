package com.example.myapplication10;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;

/*---------to include all fragments--------*/
import com.example.myapplication10.Fragment.*;

import com.example.myapplication10.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
{
    ActivityMainBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        /*------------------------Making View Binding-----------------*/
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*------------------------Making ToolBar---------------------*/
        setSupportActionBar(binding.toolbar);
        MainActivity.this.setTitle("My Profile");
        binding.toolbar.setVisibility(View.GONE);

        /*---------------------Making Fragment Manager and transaction to control Fragments------------------*/
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        /*-----------------Default Fragment-------------------*/
        transaction.add(R.id.container, new HomeFragment());
        transaction.commit();

        binding.bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                int id = menuItem.getItemId();

                /*----------------Checking for which icon is clicked in navigation bar--------------*/
                if(id == R.id.navigation_home) {
                    binding.toolbar.setVisibility(View.GONE);
                    selectedFragment = new HomeFragment();
                }
                else if(id == R.id.navigation_notification) {
                    binding.toolbar.setVisibility(View.GONE);
                    selectedFragment = new NotificationFragment();
                }
                else if(id == R.id.navigation_add) {
                    binding.toolbar.setVisibility(View.GONE);
                    selectedFragment = new AddFragment();
                }
                else if(id == R.id.navigation_search) {
                    binding.toolbar.setVisibility(View.GONE);
                    selectedFragment = new SearchFragment();
                }
                else if(id == R.id.navigation_profile) {
                    binding.toolbar.setVisibility(View.VISIBLE);
                    selectedFragment = new ProfileFragment();
                }

                /*-----------------Replacing fragment w.r.t id---------------------*/
                if (selectedFragment != null)
                {
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container, selectedFragment);
                    transaction.commit();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.profile_setting_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.setting){
            auth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}