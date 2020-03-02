package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private DevicesFragment deviceFragment;

    private Context context;

    private List<String> devicesIdList;

    //此处为提供给devicefragment的接口，使其在点击某设备的关闭按钮后显示alertDialog以进行确认
    public static interface OnItemSwitchOffButtonClickeListener {
        public void onItemSwitchOffButtonClicked(String deviceId);
    }

    //viewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView deviceIdText;

        Button switchOffButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIdText = itemView.findViewById(R.id.device_id);
            switchOffButton = itemView.findViewById(R.id.button_switch_off);
        }
    }

    //初始化
    public DeviceAdapter(List<String> idList, DevicesFragment devicesFragment) {
        devicesIdList = idList;
        deviceFragment = devicesFragment;
    }

    //舒适化界面
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.devices_frag_device_item, parent, false);
        return new ViewHolder(view);
    }

    //当显示某个device时调用
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String id = devicesIdList.get(position);
        holder.deviceIdText.setText(id);
        holder.switchOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceFragment.onItemSwitchOffButtonClicked(id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devicesIdList.size();
    }
}
