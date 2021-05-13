package com.example.discustrainer;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.discustrainer.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity {

    private final String deviceName = "Bluefruit52";
    private BluetoothGatt gattDevice = null;
    private static final UUID kUartServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID kUartTxCharacteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID kUartRxCharacteristicUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private int mUartTxCharacteristicWriteType;
    private BluetoothGattCharacteristic mUartTxCharacteristic;
    private BluetoothGattCharacteristic mUartRxCharacteristic;
    Button getBtn;
    Button saveBtn;
    TextView altitudeText;
    TextView longitudeText;
    TextView latitudeText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        SmartDisc disc = new SmartDisc(this, deviceName);
        DiscusData discDataManager = new DiscusData();
        disc.addDataListener(discDataManager);

        getBtn = (Button) findViewById(R.id.sendID);
        getBtn.setOnClickListener(v -> {
            byte[] data = {(byte)170, (byte)1};
            disc.write(data);
        });

        saveBtn = (Button) findViewById(R.id.saveID);
        saveBtn.setOnClickListener(v -> {
            discDataManager.toCsv(getApplicationContext());
        });
    }

    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("connect", "connected");
                        gattDevice = gatt;
                        gattDevice.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("connect", "services found");
                        for(BluetoothGattService service : gatt.getServices()){
                            Log.d("connect", "new services" + service.getUuid());
                        }

                        BluetoothGattService service = gattDevice.getService(kUartServiceUUID);
                        if(service!=null) {
                            mUartTxCharacteristic = service.getCharacteristic(kUartTxCharacteristicUUID);
                            mUartRxCharacteristic = service.getCharacteristic(kUartRxCharacteristicUUID);
                            if (mUartTxCharacteristic != null && mUartRxCharacteristic != null) {
                                Log.d("connect", "Uart Enable for: " + gatt.getDevice().getName());

                                BluetoothGattDescriptor descriptor = mUartRxCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                                if (gattDevice != null && descriptor != null && (mUartRxCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                    gattDevice.setCharacteristicNotification(mUartRxCharacteristic, true);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    final boolean success2 = gattDevice.writeDescriptor(descriptor);
                                    Log.d("sending data2", String.valueOf(success2));
                                    //characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                } else {
                                    Log.d("FAILED", "enable notify: client config descriptor not found for characteristic: " + mUartRxCharacteristic.getUuid().toString());
                                }

//                                mUartTxCharacteristicWriteType = mUartTxCharacteristic.getWriteType();
//                                mUartTxCharacteristic.setWriteType(mUartTxCharacteristicWriteType);
//                                mUartTxCharacteristic.setValue("1");
//                                final boolean success = gattDevice.writeCharacteristic(mUartTxCharacteristic);
//                                Log.d("sending data", String.valueOf(success));
                            }
                        }
                    } else {
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                    Log.d("charchange", ""+ characteristic.getStringValue(0));

                }

                @Override
                public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
                    Log.d("write", "finished");
                }
            };

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            Log.d("scan", "single found");
        }


        public void onBatchScanResults(List<ScanResult> results) {
            Log.d("scan", String.valueOf(results.size()));
            for(ScanResult result : results){
                try {
                    if(result.getDevice().getName().equals(deviceName)){
                        Log.d("scan", "device found");
                        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                        scanner.stopScan(mScanCallback);
                        connect(result.getDevice());
                    }
//                    Log.d("scan", result.getDevice().getName());
//                    Log.d("scan", deviceName);
                }catch(Exception e) {
                    //  Block of code to handle errors
                }
            }

        }

        public void onScanFailed(int errorCode) {
        }
    };

    private void connect(BluetoothDevice device){
        device.connectGatt(this, false, gattCallback);
    }

    private synchronized void startWithFilters(@Nullable List<ScanFilter> filters) {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false).build();
        try {
            scanner.startScan(filters, settings, mScanCallback);
        } catch (IllegalStateException e) {     // Exception if the BT adapter is not on
            Log.d("scan", "startWithFilters illegalStateExcpetion" + e.getMessage());
        }
    }

    @Override
    public void onStop() {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(mScanCallback);
        gattDevice.close();
        gattDevice = null;
        super.onStop();
    }

}