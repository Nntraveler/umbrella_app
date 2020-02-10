package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.myapplication.database.Device;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.litepal.LitePal;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private TextView toolbarTitle;

    private NavigationView navigationView;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view_drawer);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        toolbarTitle = findViewById(R.id.toolbar_title);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_item_location) {
                    Intent intent = new Intent(MainActivity.this, ChooseAreaActivity.class);
                    startActivity(intent);
                    return true;
                }
                drawerLayout.closeDrawers();
                menuItem.setChecked(false);
                return true;
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_weather:
                        toolbarTitle.setText("天气");
                        replaceFragment(new WeatherFragment());
                        break;
                    case R.id.nav_lock:
                        toolbarTitle.setText("关锁");
                        replaceFragment(new LockFragment());
                        break;
                    case R.id.nav_devices:
                        toolbarTitle.setText("设备");
                        replaceFragment(new DevicesFragment());
                        break;
                }
                return true;
            }
        });
        replaceFragment(new WeatherFragment());
        checkDevices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
        toolbarTitle.setText("天气");
        replaceFragment(new WeatherFragment());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.layout_fragment, fragment);
        transaction.commit();
    }

    //仅用于显示
    private void checkDevices() {
        LitePal.deleteAll(Device.class);
        Device device = new Device();
        device.setDeviceId("1354651");
        device.save();
        device.clearSavedState();
        device.setDeviceId("98465123");
        device.save();
        device.clearSavedState();
        device.setDeviceId("saefhsdfn");
        device.save();
    }
}
