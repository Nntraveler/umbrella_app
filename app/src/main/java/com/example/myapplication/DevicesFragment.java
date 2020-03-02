package com.example.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.database.Device;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DevicesFragment extends Fragment implements DeviceAdapter.OnItemSwitchOffButtonClickeListener {

    private List<String> devicesIdList = new ArrayList<>();

    private DeviceAdapter adapter;

    private ProgressDialog progress;

    private BluetoothSocket btSocket = null;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Boolean isConnected = false;

    //舒适化界面
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        initDevicesIdList();
        //设置device对应的recyclerView
        RecyclerView devicesRecyclerView = view.findViewById(R.id.devices_frag_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        devicesRecyclerView.setLayoutManager(layoutManager);
        adapter = new DeviceAdapter(devicesIdList, this);
        devicesRecyclerView.setAdapter(adapter);
        return view;
    }

    //当点击某个设备对应的关闭按钮，需要弹出alertDialog进行确认
    @Override
    public void onItemSwitchOffButtonClicked(String deviceId) {
        showAlertDialog(deviceId);
    }

    //TODO：在mainactivity初始化时加入了几组测试数据测试UI，之后需要删掉
    private void initDevicesIdList() {
        List<Device> devices = LitePal.findAll(Device.class);
        for (int i = 0; i < devices.size(); ++i) {
            devicesIdList.add(devices.get(i).getDeviceMACaddress());
        }
    }

    //是否确认开锁的alertDialog，若是，打开对应id的设备
    private void showAlertDialog(final String deviceIdToUnlock) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("开锁确认");
        dialog.setMessage("是否打开ID为" + deviceIdToUnlock + "的设备？");
        dialog.setCancelable(false);
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                unlockDevice(deviceIdToUnlock);
            }
        });
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }

    //TODO：开锁逻辑，先开锁，后将本地数据库内的对应的设备删除
    private void unlockDevice(String deviceIdToUnlock) {

        //TODO:向服务器发送开锁请求
        new ConnectBT(deviceIdToUnlock).execute();
        if(!isConnected){
            msg("Failed to connect. Please make sure you are around the locker!");
            return;
        }
        if(sendSignal("1")) {
            int position = devicesIdList.indexOf(deviceIdToUnlock);
            devicesIdList.remove(position);
            LitePal.deleteAll(Device.class, "deviceId = ?", deviceIdToUnlock);
            adapter.notifyItemRemoved(position);
        }
        else{
            msg("fail to open locker!");
        }
    }
    private Boolean sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {
                btSocket.getOutputStream().write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
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
            } catch(IOException e) {
                msg("Error");
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {


        private boolean ConnectSuccess = true;

        private String lockerMACAddress = "";

        @Override
        protected  void onPreExecute () {
            progress = ProgressDialog.show(getContext(), "Connecting...", "Please Wait!!!");
        }

        public ConnectBT(String lockerMACAddress) {
            this.lockerMACAddress = lockerMACAddress;
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isConnected ) {
                    BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
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
}
