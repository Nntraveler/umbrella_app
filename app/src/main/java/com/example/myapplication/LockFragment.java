package com.example.myapplication;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
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

public class LockFragment extends Fragment implements View.OnClickListener {

    private ImageButton scanQRcodeToLock;

    private ProgressDialog progress;

    private BluetoothSocket btSocket = null;

    private BluetoothAdapter myBluetooth = null;

    private Boolean isConnected = false;

    private String deviceName;

    private EditText deviceId;

    private Button lockButton;

    private static final int REQUEST_ENABLE_BT = 2;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String lockerMACAddress = "";

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
                //TODO: 检测是否已经匹配了一个设备了
                scanQRcode();

                break;
            case R.id.lock_frag_lock_button:
                if(!isConnected) {
                    msg("Please first connect to lockers!");
                }
                lockDevice();
                deviceId.setText("");
                break;
        }
    }


    //TODO：添加设备
    private int pairLocker(String lockerName) {

        if(lockerName.isEmpty()){
            // 扫描失败
            msg("QRcode is not valid!");
            return -1;
        }
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            //设备不支持蓝牙 报错
            return -1;
        }
        if(!myBluetooth.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        String deviceHardwareAddress = "";
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if(deviceName.equals(lockerName)) {
                    deviceHardwareAddress = device.getAddress(); // MAC address
                    break;
                }
            }
        }
        if(deviceHardwareAddress.isEmpty()){
            msg("Can't find the device! ");
            return -1;
        }
        msg("test2");
        new ConnectBT(deviceHardwareAddress).execute();
        return 0;
    }

    private void sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {
                btSocket.getOutputStream().write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void msg (String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }

    private void Disconnect () {
        if ( btSocket!=null ) {
            try {
                btSocket.close();
                isConnected = false;
            } catch(IOException e) {
                msg("Error");
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        private String MACAddress = "";

        public ConnectBT(String MACAddress) {
            this.MACAddress = MACAddress;
        }

        @Override
        protected  void onPreExecute () {
            msg("connecting");
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null && !isConnected ) {
                    BluetoothDevice locker = myBluetooth.getRemoteDevice(MACAddress);
                    btSocket = locker.createInsecureRfcommSocketToServiceRecord(myUUID);
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
            } else {
                msg("Connected");
                isConnected = true;
            }

        }
    }



    //关锁函数先请求后将对应的设备ID保存到本地数据库
    private void lockDevice() {
        sendSignal("2");
        Device device = new Device();
        device.setDeviceName(deviceName);
        device.save();
        msg("Close locker!");
        Disconnect();
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
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //TODO:解析并发送请求
                String lockerName = result.getContents();
                // Not a valid mac address
                if(BluetoothAdapter.checkBluetoothAddress(lockerName)) {
                    Toast.makeText(getContext(),"QR code is not valid", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getContext(), "Scanned: " + result.getContents(),
                        Toast.LENGTH_LONG).show();

                deviceName = lockerName;
                pairLocker(lockerName);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
