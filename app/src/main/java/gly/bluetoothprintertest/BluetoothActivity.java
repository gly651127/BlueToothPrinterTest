package gly.bluetoothprintertest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

public class BluetoothActivity extends Activity {
    private Context mContext = null;
    private SwitchCompat switchBT;
    public BluetoothConnectUtil bluetoothService;
    private Button print;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setTitle("蓝牙打印");
        setContentView(R.layout.bluetooth_layout);
        this.initListener();
        bluetoothService = new BluetoothConnectUtil(this);
        boolean isOpenBlueTooth = SharedPreferencesUtil.getBoolean(mContext, "isOpenBlueTooth");
        switchBT.setChecked(isOpenBlueTooth);
        if (isOpenBlueTooth) {
            if (!bluetoothService.isOpen()) {
                // 蓝牙关闭的情况
                System.out.println("蓝牙关闭的情况");
                bluetoothService.openBluetooth(BluetoothActivity.this);
            } else {
                bluetoothService.searchDevices();
            }
        }
    }

    private void initListener() {
        switchBT = this.findViewById(R.id.openBluetooth_tb);
        print = this.findViewById(R.id.print);
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothService.print();
            }
        });
        switchBT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferencesUtil.putBoolean(mContext, "isOpenBlueTooth", b);
                if (b) {
                    if (!bluetoothService.isOpen()) {
                        // 蓝牙关闭的情况
                        System.out.println("蓝牙关闭的情况");
                        bluetoothService.openBluetooth(BluetoothActivity.this);
                    } else {
                        bluetoothService.searchDevices();
                    }
                } else {
                    PrintDataUtil.disconnect();
                }

            }
        });
    }

    //屏蔽返回键的代码:    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        PrintDataUtil.disconnect();
        super.onDestroy();
    }
}  