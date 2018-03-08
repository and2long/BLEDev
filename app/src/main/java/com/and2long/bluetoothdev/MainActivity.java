package com.and2long.bluetoothdev;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<DevicesBean> mData = new ArrayList<>();
    private DeviceAdapter adapter;
    private static final int REQUEST_CODE_LOCATION = 100;
    private Handler mHandler = new Handler();
    private List<String> mLogData = new ArrayList<>();
    private LogAdapter logAdapter;
    private RecyclerView logList;
    boolean mScanning = false;
    int SCAN_PERIOD = 10000;
    public BluetoothGatt mBluetoothGatt;

    private BluetoothGattCallback mBluetoothGattCallback;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initLogView();
        initDevideView();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //强制开启蓝牙，不予提示。
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

    }

    private void init() {
        //状态改变
        mBluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                log(DateUtil.getCurrentDateFormat() + "\n" + "onConnectionStateChange() called with: " +
                        "gatt = [" + gatt + "], status = [" + status + "], newState = [" + newState + "]");

                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        log("STATE_CONNECTED");
                        refreshDeviceListState(Constants.STATE_CONNECTED);
                        gatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        log("STATE_DISCONNECTED");
                        refreshDeviceListState(Constants.STATE_DISCONNECTED);
                        gatt.close();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                String e1 = DateUtil.getCurrentDateFormat() + "\n" + "onServicesDiscovered() called with: " +
                        "gatt = [" + gatt + "], status = [" + status + "]";
                log(e1);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //初始化监听特征码
                    initCharacteristic();
                    try {
                        Thread.sleep(200);//延迟发送，否则第一次消息会不成功
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                String e = DateUtil.getCurrentDateFormat() + "\n" + "onCharacteristicWrite() called with: " +
                        "gatt = [" + gatt + "], characteristic = [" + characteristic + "], status = [" + status + "]";
                log(e);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                String e = DateUtil.getCurrentDateFormat() + "\n" + "onDescriptorWrite() called with: " +
                        "gatt = [" + gatt + "], descriptor = [" + descriptor + "], status = [" + status + "]";
                log(e);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                String e = DateUtil.getCurrentDateFormat() + "\n" + "onDescriptorRead() called with: " +
                        "gatt = [" + gatt + "], descriptor = [" + descriptor + "], status = [" + status + "]";
                log(e);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                String s = DateUtil.getCurrentDateFormat() + "\n" + "onCharacteristicRead() called with: " +
                        "gatt = [" + gatt + "], characteristic = [" + characteristic + "], status = [" + status + "]";
                log(s);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                String s = DateUtil.getCurrentDateFormat() + "\n" + "onCharacteristicChanged() " +
                        "called with: gatt = [" + gatt + "], characteristic = [" + characteristic + "]";
                log(s);
                readCharacteristic(characteristic);
            }

        };
        //蓝牙扫描回调
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                String name = device.getName();
                log(DateUtil.getCurrentDateFormat() + "\n" + name + " : " + device.getAddress());
                if (mData.size() > 0) {
                    for (DevicesBean bean : mData) {
                        if (bean.getDevice().equals(device)) {
                            break;
                        } else {
                            addToDeviceList(device, name);
                        }
                    }
                } else {
                    addToDeviceList(device, name);
                }

                adapter.notifyDataSetChanged();

            }

            private void addToDeviceList(BluetoothDevice device, String name) {
                if (name != null && !name.contains("iMac") && !name.contains("Apple")) {
                    DevicesBean devicesBean = new DevicesBean();
                    devicesBean.setDevice(device);
                    devicesBean.setState(Constants.STATE_DISCONNECTED);
                    mData.add(devicesBean);
                }
            }
        };
    }

    private void initDevideView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new DeviceAdapter(this, mData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                stopScan();
                String e = DateUtil.getCurrentDateFormat() + "\n" + "尝试连接：" + mData.get(position).getDevice().getName()
                        + "\n" + "*****稍等一会，等日志刷新后再次操作*****"
                        + "\n" + "####如果日志出现“error status：133”，请重新扫描BLE列表！####";
                log(e);
                //第二个参数为false时，尝试连接一次。
                mBluetoothGatt = mData.get(position).getDevice().connectGatt(
                        MainActivity.this, false, mBluetoothGattCallback);
                refreshDeviceListState(Constants.STATE_CONNECTING);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
    }

    /**
     * 点击某一个设备后，刷新其在列表中的链接状态值。
     *
     * @param state 连接状态
     */
    private void refreshDeviceListState(int state) {
        for (DevicesBean devicesBean : mData) {
            if (devicesBean.getDevice().getName().equals(mBluetoothGatt.getDevice().getName())) {
                devicesBean.setState(state);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }

    }

    private void initLogView() {
        logList = findViewById(R.id.recyclerView1);
        logAdapter = new LogAdapter(this, mLogData);
        logList.setLayoutManager(new LinearLayoutManager(this));
        logList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        logList.setAdapter(logAdapter);
    }

    private void scan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                scanLeDevice();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
            }
        } else {
            scanLeDevice();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_scan:
                scan();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanLeDevice();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.alert_permission_location)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //申请定位权限
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(
                                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                    REQUEST_CODE_LOCATION);
                                        }
                                    }
                                }).show();
                    }
                }
            }
        }
    }

    /**
     * 扫描设备
     */
    private void scanLeDevice() {
        mData.clear();
        adapter.notifyDataSetChanged();
        setTitle(R.string.scanning);
        // Stops scanning after a pre-defined scan period.
        // 预先定义停止蓝牙扫描的时间（因为蓝牙扫描需要消耗较多的电量）
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
        mScanning = true;

        // 定义一个回调接口供扫描结束处理
        mBluetoothAdapter.startLeScan(leScanCallback);
        String e = DateUtil.getCurrentDateFormat() + "\n" + getString(R.string.start_scan);
        log(e);

    }

    private void stopScan() {
        if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(leScanCallback);
            setTitle(getString(R.string.end_scan));
            String e = DateUtil.getCurrentDateFormat() + "\n" + getString(R.string.end_scan);
            log(e);
            if (mData.size() == 0) {
                Toast.makeText(this, R.string.no_device_found, Toast.LENGTH_LONG).show();
            }
        }
    }


    private void log(String msg) {
        mLogData.add(msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAdapter.notifyDataSetChanged();
                logList.scrollToPosition(logAdapter.getItemCount() == 0 ? 0 : logAdapter.getItemCount() - 1);
            }
        });
        writeFile(msg);
        Log.i(TAG, "log: " + msg);
    }

    public synchronized void initCharacteristic() {
        if (mBluetoothGatt == null)
            throw new NullPointerException();
        List<BluetoothGattService> services = mBluetoothGatt.getServices();
        //因为不知道对方的UUID，这里遍历把所有的服务全部注册
        log("------Start Collecting Device Info------");
        log(DateUtil.getCurrentDateFormat());
        for (int i = 0; i < services.size(); i++) {
            BluetoothGattService service = services.get(i);
            log("service" + i + ":" + String.valueOf(service.getUuid()));
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (characteristics != null) {
                for (int y = 0; y < characteristics.size(); y++) {
                    BluetoothGattCharacteristic characteristic = characteristics.get(y);
                    UUID uuid = characteristic.getUuid();
                    log("characteristic" + y + ":Uuid:" + String.valueOf(uuid));
                    log("characteristic" + y + ":Permissions:" + characteristic.getPermissions());
                    log("characteristic" + y + ":WriteType:" + characteristic.getWriteType());
                    log("characteristic" + y + ":Discriptors size::" + characteristic.getDescriptors().size());
                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                    if (descriptors.size() > 0) {
                        for (int x = 0; x < descriptors.size(); x++) {
                            BluetoothGattDescriptor descriptor = descriptors.get(x);
                            log("descriptor" + x + ": Uuid:" + descriptor.getUuid());
                            log("descriptor" + x + ": Value:" + Arrays.toString(descriptor.getValue()));
                            log("descriptor" + x + ": Permissions:" + descriptor.getPermissions());
                        }
                    }
                }
            }
        }
        log("------End Collecting Device Info------");

        //设置监听所有服务
        for (BluetoothGattService service : services) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (characteristics != null) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String e = DateUtil.getCurrentDateFormat() + "\n" + "Monitor Service："
                            + String.valueOf(service.getUuid() + "\n" + "Monitor characteristic："
                            + String.valueOf(characteristic.getUuid()));
                    log(e);
                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                }
            }
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            String msg = "BluetoothAdapter not initialized";
            log(msg);
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
        byte[] bytes = characteristic.getValue();
        String str = new String(bytes);
        String msg = "## --------Receiver Data-------- " + "\n"
                + "String:" + str + "\n"
                + "byte[]:" + Arrays.toString(bytes);
        log(msg);
    }

    /**
     * 关闭
     */
    public void closeGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private void writeFile(String msg) {
        FileWriter fw = null;
        File file = new File(getExternalCacheDir(), "device_data.txt");
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, true);
            fw.append(msg + "\n");
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
