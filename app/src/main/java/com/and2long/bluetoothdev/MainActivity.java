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

import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mData = new ArrayList<>();
    private MAdapter adapter;
    private static final int REQUEST_CODE_LOCATION = 100;
    private Handler mHandler = new Handler();
    final BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            if (!mData.contains(device)) {
                mData.add(device);
            }
            adapter.notifyDataSetChanged();

            Log.i(TAG, "run: scanning..." + device.getName() + "," + device.getAddress());
        }
    };
    private BluetoothGattCharacteristic characteristicRead;
    private BluetoothGattCharacteristic characteristicWrite;
    private UUID UUID_SERVER = UUID.fromString("8b624661-89ea-4ec9-97cc-eda0b52e96e6");
    private UUID UUID_CHARREAD = UUID.fromString("8b624662-89ea-4ec9-97cc-eda0b52e96e6");
    private UUID UUID_DESCRIPTOR = UUID.fromString("8b624663-89ea-4ec9-97cc-eda0b52e96e6");
    private UUID UUID_CHARWRITE = UUID.fromString("8b624664-89ea-4ec9-97cc-eda0b52e96e6");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        adapter = new MAdapter(this, mData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                //第二个参数为false时，尝试连接一次。
                mBluetoothGatt = mData.get(position).connectGatt(MainActivity.this, true, mBluetoothGattCallback);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //强制开启蓝牙，不予提示。
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

    }

    private void scan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                scanLeDevice(true);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
            }
        } else {
            scanLeDevice(true);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanLeDevice(true);
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
                                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
                                        }
                                    }
                                }).show();
                    }
                }
            }
        }
    }

    boolean mScanning = false;
    int SCAN_PERIOD = 3000;

    /**
     * 定时扫描
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mData.clear();
            adapter.notifyDataSetChanged();
            setTitle(R.string.scanning);
            // Stops scanning after a pre-defined scan period.
            // 预先定义停止蓝牙扫描的时间（因为蓝牙扫描需要消耗较多的电量）
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(callback);
                    setTitle("扫描结束");
                }
            }, SCAN_PERIOD);
            mScanning = true;

            // 定义一个回调接口供扫描结束处理
            mBluetoothAdapter.startLeScan(callback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(callback);
        }
    }

    public BluetoothGatt mBluetoothGatt;

    //    状态改变
    BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.i(TAG, "onConnectionStateChange: thread " + Thread.currentThread() + " status " + newState);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                String err = "Cannot connect device with error status: " + status;
                // 当尝试连接失败的时候调用 disconnect 方法是不会引起这个方法回调的，所以这里
                //   直接回调就可以了。
                gatt.close();
                Log.i(TAG, err);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                Log.i(TAG, "connect--->success" + newState + "," + gatt.getServices().size());
//                setState(ConnectionState.STATE_CONNECTING);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");

                Log.i(TAG, "connect--->failed" + newState);
//                setState(ConnectionState.STATE_NONE);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered received:  SUCCESS");
//                setState(ConnectionState.STATE_CONNECTED);
                initCharacteristic();
                try {
                    Thread.sleep(200);//延迟发送，否则第一次消息会不成功
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "onServicesDiscovered error falure " + status);
//                setState(ConnectionState.STATE_NONE);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "onCharacteristicWrite status: " + status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG, "onDescriptorWrite status: " + status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(TAG, "onDescriptorRead status: " + status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "onCharacteristicRead status: " + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged characteristic: " + characteristic);
            readCharacteristic(characteristic);
        }

    };


    public synchronized void initCharacteristic() {
        if (mBluetoothGatt == null)
            throw new NullPointerException();
        List<BluetoothGattService> services = mBluetoothGatt.getServices();
        Log.i(TAG, services.toString());
        BluetoothGattService service = mBluetoothGatt.getService(UUID_SERVER);
        characteristicRead = service.getCharacteristic(UUID_CHARREAD);
        characteristicWrite = service.getCharacteristic(UUID_CHARWRITE);

        if (characteristicRead == null)
            throw new NullPointerException();
        if (characteristicWrite == null)
            throw new NullPointerException();
        mBluetoothGatt.setCharacteristicNotification(characteristicRead, true);
        BluetoothGattDescriptor descriptor = characteristicRead.getDescriptor(UUID_DESCRIPTOR);
        if (descriptor == null)
            throw new NullPointerException();
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
        byte[] bytes = characteristic.getValue();
        String str = new String(bytes);
//        mHandler.obtainMessage(READ_MESSAGE, str).sendToTarget();
        Log.i(TAG, "## readCharacteristic, 读取到: " + str);
    }

    public void write(byte[] cmd) {
        Log.i(TAG, "write:" + new String(cmd));
        if (cmd == null || cmd.length == 0)
            return;
        //        synchronized (LOCK) {
        characteristicWrite.setValue(cmd);
        characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.writeCharacteristic(characteristicWrite);
        Log.i(TAG, "write:--->" + new String(cmd));
        //        }
    }

    /**
     * 关闭
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
//        setState(ConnectionState.STATE_NONE);
    }


//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            // When discovery finds a device
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // 获取设备对象
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                //去重
//                if (!mData.contains(device)) {
//                    mData.add(device);
//                }
//                adapter.notifyDataSetChanged();
//                // When discovery is finished, change the Activity title
//            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                setProgressBarIndeterminateVisibility(false);
//                if (adapter.getItemCount() != 0) {
//                    setTitle(R.string.select_device);
//                } else {
//                    setTitle(R.string.none_found);
//                }
//            }
//        }
//    };
}
