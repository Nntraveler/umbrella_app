package com.example.myapplication;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.*;

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

import static android.app.Activity.RESULT_OK;

public class LockFragment extends Fragment implements View.OnClickListener {

    private ImageButton scanQRcodeToLock;

    private ProgressDialog progress;

    private BluetoothSocket btSocket = null;

    private BluetoothAdapter myBluetooth = null;

    private Boolean isConnected = false;

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

                if(pairLocker() >= 0){
                    lockDevice(lockerMACAddress);
                }

                break;
            case R.id.lock_frag_lock_button:
                if(!isConnected) {
                    msg("no paired lockers!");
                }
                lockDevice(lockerMACAddress);
                deviceId.setText("");
                break;
        }
    }


    //TODO：添加设备
    private int pairLocker() {

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            //设备不支持蓝牙 报错
            return -1;
        }
        if(!myBluetooth.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        scanQRcode();
        if(lockerMACAddress.isEmpty()){
            // 扫描失败
            msg("QRcode is not valid!");
            return -1;
        }
        Device device = new Device();
        device.setDeviceMACaddress(lockerMACAddress);
        device.save();

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
            } catch(IOException e) {
                msg("Error");
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected  void onPreExecute () {
            progress = ProgressDialog.show(getContext(), "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isConnected ) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice locker = myBluetooth.getRemoteDevice(lockerMACAddress);
                    btSocket = locker.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
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

            progress.dismiss();
        }
    }



    //关锁函数先请求后将对应的设备ID保存到本地数据库
    private void lockDevice(String deviceMACAddress) {
        if (deviceAvailable(deviceMACAddress) && isConnected) {
            new ConnectBT().execute();
            if(!isConnected){
                msg("Fail to open locker!");
                return; //连接失败
            }
            sendSignal("2");
            msg("Close locker!");
            Disconnect();
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
                String macAddress = result.getContents();
                // Not a valid mac address
                if(!Pattern.matches("[0-9a-f]{2}([-:]?)[0-9a-f]{2}(\\1[0-9a-f]{2}){4}$",macAddress.toLowerCase())) {
                    Toast.makeText(getContext(),"QR code is not valid", Toast.LENGTH_LONG).show();
                    return;
                }
                lockerMACAddress = macAddress;

                Toast.makeText(getContext(), "Scanned: " + result.getContents(),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
