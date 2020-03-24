package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.myapplication.comunicator.Comunicator;
import com.rengwuxian.materialedittext.MaterialEditText;


public class LoginFragment extends Fragment {

    public static final int SUCCEED = 1;

    public static final int INVALID_ACCOUNT = -1;

    public static final int INVALID_PASSWORD = 0;

    public static final int CONNECTION_FAIL = 4;

    private Comunicator comunicator = new Comunicator();

    private Boolean isConnected = false;

    private Button loginButton;

    private Button signUpLink;

    private MaterialEditText usernameEditText;

    private MaterialEditText passwordEditText;

    private AppCompatCheckBox autoLoginCheckbox;

    private LoginActivity loginActivity;

    public static interface OnSignUpLinkClickListener {
        public void onSignUpLinkClick();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private int checkAccount(String username, String password) {
        if (username.isEmpty()) {
            return INVALID_ACCOUNT;
        } else if (password.isEmpty()) {
            return INVALID_PASSWORD;
        }

        if(isConnected || comunicator.connect()){
            isConnected = true;
            if(!comunicator.signIn(username, password)){
                return INVALID_PASSWORD;
            }
        }
        else{
            return CONNECTION_FAIL;
        }
        //TODO：检查账号是否存在，密码是否正确。若账号不存在返回 INVALID_ACCOUNT，若密码不正确，返回 INVALID_PASSWORD, 否则返回SUCCEED

        return SUCCEED;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        loginActivity = (LoginActivity) getActivity();

        loginButton = view.findViewById(R.id.button_login);
        signUpLink = view.findViewById(R.id.button_signup_link);
        usernameEditText = view.findViewById(R.id.edit_username);
        passwordEditText = view.findViewById(R.id.edit_password);
        autoLoginCheckbox = view.findViewById(R.id.checkbox_auto_login);
        signUpLink = view.findViewById(R.id.button_signup_link);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String username = usernameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();

                    switch (checkAccount(username.trim(), password.trim())) {
                        case SUCCEED:
                            login(username, password);
                            break;
                        case INVALID_ACCOUNT:
                            usernameEditText.setError("账号不存在");
                            break;
                        case INVALID_PASSWORD:
                            passwordEditText.setError("密码错误");
                            break;
                        case CONNECTION_FAIL:
                            usernameEditText.setError("网络连接失败");
                            break;
                    }
                    if(isConnected){
                        comunicator.close();
                        isConnected = false;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginActivity.onSignUpLinkClick();
            }
        });

        return view;
    }

    private void login(String username, String password) {
        if (autoLoginCheckbox.isChecked()) {
            SharedPreferences.Editor editor = loginActivity.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
            editor.putString("USERNAME", username);
            editor.putString("PASSWORD", password);
            editor.putBoolean("AUTO_LOGIN", true);
            editor.apply();
        }
        loginActivity.startMainActivity(username);
    }
}
