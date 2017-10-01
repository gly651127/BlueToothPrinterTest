package gly.bluetoothprintertest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

import gly.bluetoothprintertest.baseListadapter.CommonAdapter;
import gly.bluetoothprintertest.baseListadapter.ViewHolder;

public class BluetoothConnectUtil {
    private Context context = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private ArrayList<BluetoothDevice> devices = null;
    private PrintDataUtil printDataService;


    /**
     * 绑定蓝牙设备
     */
    private void bondDevice(int position) {
        try {
            Method createBondMethod = BluetoothDevice.class
                    .getMethod("createBond");
            createBondMethod
                    .invoke(devices.get(position));
        } catch (Exception e) {
            Toast.makeText(context, "配对失败！", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public BluetoothConnectUtil(Context context) {
        this.context = context;
        this.devices = new ArrayList<>();
        this.initIntentFilter();

    }

    private void initIntentFilter() {
        // 设置广播信息过滤    
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果    
        context.registerReceiver(receiver, intentFilter);

    }

    /**
     * 打开蓝牙
     */
    public void openBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, 1);

    }

    /**
     * 关闭蓝牙
     */
    public void closeBluetooth() {
        this.bluetoothAdapter.disable();
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return boolean
     */
    public boolean isOpen() {
        return this.bluetoothAdapter.isEnabled();

    }

    /**
     * 搜索蓝牙设备
     */
    public void searchDevices() {
        this.devices.clear();
        // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
        this.bluetoothAdapter.startDiscovery();
    }

    /**
     * 添加蓝牙设备到list集合
     *
     * @param device
     */
    public void addDevices(BluetoothDevice device) {
        System.out.println("未绑定设备名称：" + device.getName());
        if (!this.devices.contains(device)) {
            this.devices.add(device);
        }
    }


    /**
     * 蓝牙广播接收器
     */

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        AlertDialog dialog;
        AlertDialog.Builder builder;

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("onReceive" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().startsWith("Printer_")) {
                    addDevices(device);
                    bluetoothAdapter.cancelDiscovery();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                builder = new AlertDialog.Builder(context).setTitle("搜索蓝牙设备中...");
                dialog = builder.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                System.out.println("设备搜索完毕");
                dialog.dismiss();
                if (devices.size() == 0) {
                    builder.setTitle("没有找到打印机").setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                } else {
                    CommonAdapter<BluetoothDevice> commonAdapter = new CommonAdapter<BluetoothDevice>(context, devices, R.layout.device_item) {
                        @Override
                        public void convert(ViewHolder helper, BluetoothDevice item) {
                            helper.setText(R.id.device_name, item.getName());
                            helper.setText(R.id.device_state, item.getBondState() == BluetoothDevice.BOND_BONDED ? "已绑定" : "未绑定");
                        }
                    };
                    builder.setTitle("搜索完成").setAdapter(commonAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.out.println("click！" + i);
                            BluetoothDevice bluetoothDevice = devices.get(i);
                            if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                                bondDevice(i);
                            } else {
                                printDataService = new PrintDataUtil(context, devices.get(i).getAddress());
                                boolean connect = printDataService.connect();
                                if (connect == false) {
                                    // 连接失败
                                    System.out.println("连接失败！");
                                } else {
                                    // 连接成功
                                    System.out.println("连接成功！");

                                }
                            }
                        }
                    });
                }
                dialog = builder.show();
                // bluetoothAdapter.cancelDiscovery();
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    System.out.println("--------打开蓝牙-----------");
                    searchDevices();
                } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    System.out.println("--------关闭蓝牙-----------");
                }
            }
        }
    };


    public void print() {
        if (null == printDataService) {
            return;
        }
        printDataService.selectCommand(PrintDataUtil.RESET);
        printDataService.selectCommand(PrintDataUtil.LINE_SPACING_DEFAULT);
        printDataService.selectCommand(PrintDataUtil.ALIGN_CENTER);
        printDataService.printText("美食餐厅\n\n");
        printDataService.selectCommand(PrintDataUtil.DOUBLE_HEIGHT_WIDTH);
        printDataService.printText("桌号：1号桌\n\n");
        printDataService.selectCommand(PrintDataUtil.NORMAL);
        printDataService.selectCommand(PrintDataUtil.ALIGN_LEFT);
        printDataService.printText(printDataService.printTwoData("订单编号", "201507161515\n"));
        printDataService.printText(printDataService.printTwoData("点菜时间", "2016-02-16 10:46\n"));
        printDataService.printText(printDataService.printTwoData("上菜时间", "2016-02-16 11:46\n"));
        printDataService.printText(printDataService.printTwoData("人数：2人", "收银员：张三\n"));

        printDataService.printText("--------------------------------\n");
        printDataService.selectCommand(PrintDataUtil.BOLD);
        printDataService.printText(printDataService.printThreeData("项目", "数量", "金额\n"));
        printDataService.printText("--------------------------------\n");
        printDataService.selectCommand(PrintDataUtil.BOLD_CANCEL);
        printDataService.printText(printDataService.printThreeData("面", "1", "0.00\n"));
        printDataService.printText(printDataService.printThreeData("米饭", "1", "6.00\n"));
        printDataService.printText(printDataService.printThreeData("铁板烧", "1", "26.00\n"));
        printDataService.printText(printDataService.printThreeData("一个测试", "1", "226.00\n"));
        printDataService.printText(printDataService.printThreeData("牛肉面啊啊", "1", "2226.00\n"));
        printDataService.printText(printDataService.printThreeData("牛肉面啊啊", "888", "98886.00\n"));

        printDataService.printText("--------------------------------\n");
        printDataService.printText(printDataService.printTwoData("合计", "53.50\n"));
        printDataService.printText(printDataService.printTwoData("抹零", "3.50\n"));
        printDataService.printText("--------------------------------\n");
        printDataService.printText(printDataService.printTwoData("应收", "50.00\n"));
        printDataService.printText("--------------------------------\n");

        printDataService.selectCommand(printDataService.ALIGN_LEFT);
        printDataService.printText("备注：不要辣、不要香菜");
        printDataService.printText("\n\n\n\n\n");
    }
}