package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.database.Device;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;


public class DevicesFragment extends Fragment implements DeviceAdapter.OnItemSwitchOffButtonClickeListener {

    private List<String> devicesIdList = new ArrayList<>();

    private DeviceAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        initDevicesIdList();
        RecyclerView devicesRecyclerView = view.findViewById(R.id.devices_frag_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        devicesRecyclerView.setLayoutManager(layoutManager);
        adapter = new DeviceAdapter(devicesIdList, this);
        devicesRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onItemSwitchOffButtonClicked(String deviceId) {
        showAlertDialog(deviceId);
    }

    private void initDevicesIdList() {
        List<Device> devices = LitePal.findAll(Device.class);
        for (int i = 0; i < devices.size(); ++i) {
            devicesIdList.add(devices.get(i).getDeviceId());
        }
    }

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

    private void unlockDevice(String deviceIdToUnlock) {

        //TODO:向服务器发送开锁请求

        int position = devicesIdList.indexOf(deviceIdToUnlock);
        devicesIdList.remove(position);
        LitePal.deleteAll(Device.class, "deviceId = ?", deviceIdToUnlock);
        adapter.notifyItemRemoved(position);
    }
}
