package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myapplication.database.Device;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import static android.app.Activity.RESULT_OK;

public class LockFragment extends Fragment implements View.OnClickListener {

    private ImageButton scanQRcodeToLock;

    private EditText deviceId;

    private Button lockButton;

    //初始化fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock, container, false);
        scanQRcodeToLock = view.findViewById(R.id.lock_frag_lock_imagebutton);
        deviceId = view.findViewById(R.id.lock_frag_id_text);
        lockButton = view.findViewById(R.id.lock_frag_lock_button);
        scanQRcodeToLock.setOnClickListener(this);
        lockButton.setOnClickListener(this);
        return view;
    }

    //设置button点击响应，若为二维码按键则开始扫码，若为关锁按键则关锁（仅做了UI效果）
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lock_frag_lock_imagebutton:
                scanQRcode();
                break;
            case R.id.lock_frag_lock_button:
                lockDevice(deviceId.getText().toString());
                deviceId.setText("");
                break;
        }
    }

    //TODO：扫码开锁逻辑
    private void scanQRcode() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
//        Device device = new Device();
//        device.setDeviceId("dsghklg");
//        device.save();
    }

    //关锁函数先请求后将对应的设备ID保存到本地数据库
    private void lockDevice(String deviceIdToLock) {
        if (deviceAvailable(deviceIdToLock)) {
            //TODO:发送请求
            Device device = new Device();
            device.setDeviceId(deviceIdToLock);
            device.save();
        }
    }

    //TODO：检查设备是否可用
    private boolean deviceAvailable(String deviceIdToUnlock) {
        return true;
    }

    //TODO：扫码结果处理逻辑
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                //TODO:解析并发送请求
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                deviceId.setText(result.getContents());
                Toast.makeText(getContext(), "Scanned: " + result.getContents(),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
