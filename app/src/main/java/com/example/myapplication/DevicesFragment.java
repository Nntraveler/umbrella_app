package com.example.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.database.Device;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class DevicesFragment extends Fragment {

//    private List<String> devicesIdList = new ArrayList<>();

//    private DeviceAdapter adapter;
    private MainActivity mainActivity;

    private TextView deviceIdText;

    private Button switchOffButton;

    private ProgressDialog progress;

    private String curDeviceName;

    private BluetoothSocket btSocket = null;

    private BluetoothAdapter myBluetooth;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Boolean isConnected = false;

    private static final int REQUEST_ENABLE_BT = 2;

    //舒适化界面
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        mainActivity = (MainActivity) getActivity();
        deviceIdText = view.findViewById(R.id.device_id);
        switchOffButton = view.findViewById(R.id.button_switch_off);
        curDeviceName = mainActivity.getRunningDeviceMACAddress();
        deviceIdText.setText(curDeviceName);
        switchOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog(mainActivity.getRunningDeviceMACAddress());
            }
        });
//        initDevicesIdList();
        //设置device对应的recyclerView
//        RecyclerView devicesRecyclerView = view.findViewById(R.id.devices_frag_recycler_view);
//        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
//        devicesRecyclerView.setLayoutManager(layoutManager);
//        adapter = new DeviceAdapter(devicesIdList, this);
//        devicesRecyclerView.setAdapter(adapter);
        return view;
    }

    public static interface OnDeviceSwitchOffButtonClickListener {
        public void onDeviceSwitchOffButtonClick();
    }
    //当点击某个设备对应的关闭按钮，需要弹出alertDialog进行确认


    //TODO：在mainactivity初始化时加入了几组测试数据测试UI，之后需要删掉
//    private void initDevicesIdList() {
//        List<Device> devices = LitePal.findAll(Device.class);
//        for (int i = 0; i < devices.size(); ++i) {
//            devicesIdList.add(devices.get(i).getDeviceName());
//        }
//    }

    //是否确认开锁的alertDialog，若是，打开对应id的设备
    private void showAlertDialog(final String deviceIdToUnlock) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("开锁确认");
        dialog.setMessage("是否打开ID为" + deviceIdToUnlock + "的设备？");
        dialog.setCancelable(false);
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pairDevice(deviceIdToUnlock);
            }
        });
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }

    // 连接设备
    private void pairDevice(String lockerName){
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            //设备不支持蓝牙 报错
            return;
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
            return;
        }
        new ConnectBT(deviceHardwareAddress).execute();
    }

    private void unlockDevice() {

        //TODO:向服务器发送开锁请求

        if(!isConnected){
            msg("Failed to connect. Please make sure you are around the locker!");
            return;
        }
        if(sendSignal("102X")) {
//            int position = devicesIdList.indexOf(curDeviceName);
//            devicesIdList.remove(position);
//            LitePal.deleteAll(Device.class, "deviceId = ?", curDeviceName);
//            adapter.notifyItemRemoved(position);
            SharedPreferences.Editor editor = mainActivity.getSharedPreferences("device", Context.MODE_PRIVATE).edit();
            editor.remove("RUNNING_DEVICE_MAC_ADDRESS");
            editor.apply();
            Disconnect();
            mainActivity.onDeviceSwitchOffButtonClick();
        }
        else{
            msg("fail to open locker!");
        }
    }
    private Boolean sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {
                btSocket.getOutputStream().write(number.getBytes());
            } catch (IOException e) {
                msg("Send signal Error");
                return false;
            }
            return true;
        }
        return false;
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
        protected void onPreExecute () {
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
                unlockDevice();
            }

        }
    }
}
