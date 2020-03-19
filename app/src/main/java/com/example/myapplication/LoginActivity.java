package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import static org.litepal.LitePalApplication.getContext;

public class LoginActivity extends AppCompatActivity implements LoginFragment.OnSignUpLinkClickListener, SignUpFragment.OnLoginLinkClickListener {

    private TextView toolbarTitle;

    private LoginFragment loginFragment = new LoginFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        boolean autoLogin = prefs.getBoolean("AUTO_LOGIN", false);
        String username = prefs.getString("USERNAME", null);

        if (autoLogin && username != null && !username.trim().isEmpty()) {
            startMainActivity(username);
        }

        toolbarTitle = findViewById(R.id.toolbar_title);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        toolbarTitle.setText("登录");

        replaceFragment(loginFragment);
    }

    public void onSignUpLinkClick() {
        toolbarTitle.setText("注册");
        replaceFragment(new SignUpFragment());
    }

    public void onLoginLinkClick() {
        toolbarTitle.setText("登录");
        replaceFragment(loginFragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.user_fragment, fragment);
        transaction.commit();
    }

    public void startMainActivity(String username) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }
}
