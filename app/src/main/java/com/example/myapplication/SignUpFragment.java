package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import com.example.myapplication.comunicator.Comunicator;


public class SignUpFragment extends Fragment {

    public static final int INVALID_ACCOUNT = 0;

    public static final int INVALID_PASSWORD = 1;

    public static final int INVALID_CONFIRM_PASSWORD = 2;

    public static final int SUCCEED = 3;

    public static final int CONNECTION_FAIL = 4;

    public Boolean isConnected = false;

    private MaterialEditText usernameEditText;

    private MaterialEditText passwordEditText;

    private MaterialEditText confirmPasswordEditText;

    private Button signUpButton;

    private Button loginLink;

    private LoginActivity loginActivity;

    private Comunicator comunicator = new Comunicator();

    public static interface OnLoginLinkClickListener {
        public void onLoginLinkClick();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private int checkInput(String username, String password, String confirmPassword) {
        if (username.isEmpty()) {
            return INVALID_ACCOUNT;
        } else if (password.isEmpty()) {
            return INVALID_PASSWORD;
        } else if (!confirmPassword.equals(password)) {
            return INVALID_CONFIRM_PASSWORD;
        }

        if(comunicator.connect()){
            isConnected = true;
            if(!comunicator.checkUserName(username)){
                return INVALID_ACCOUNT;
            }
        }
        else{
            return CONNECTION_FAIL;
        }

        //TODO:检查账号是否已经存在，若存在返回 INVALID_ACCOUNT，否则返回SUCCEED。

        return SUCCEED;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        loginActivity = (LoginActivity) getActivity();

        usernameEditText = view.findViewById(R.id.edit_username_signup);
        passwordEditText = view.findViewById(R.id.edit_password_signup);
        confirmPasswordEditText = view.findViewById(R.id.edit_confirm_password_signup);
        signUpButton = view.findViewById(R.id.button_signup);
        loginLink = view.findViewById(R.id.button_login_link);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String username = usernameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String confirmPassword = confirmPasswordEditText.getText().toString();

                    Toast.makeText(getContext(), password, Toast.LENGTH_SHORT).show();

                    switch (checkInput(username.trim(), password.trim(), confirmPassword.trim())) {
                        case INVALID_ACCOUNT:
                            usernameEditText.setError("账号无效或已被注册");
                            break;
                        case INVALID_PASSWORD:
                            passwordEditText.setError("密码无效");
                            break;
                        case INVALID_CONFIRM_PASSWORD:
                            confirmPasswordEditText.setError("确认密码需与密码一致");
                            break;
                        case SUCCEED:
                            comunicator.signUp(username, password);
                            loginActivity.onLoginLinkClick();
                            break;
                        case CONNECTION_FAIL:
                            usernameEditText.setError("网络连接失败");
                            break;
                    }
                    if(isConnected) {
                        comunicator.close();
                        isConnected = false;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginActivity.onLoginLinkClick();
            }
        });

        return view;
    }
}
