package com.example.discustrainer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public final class SmartDisc{
    private BluetoothGatt gattDevice;
    private String deviceName;
    private List<ScanFilter> filters = new ArrayList<>();
    private static final UUID kUartServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID kUartTxCharacteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID kUartRxCharacteristicUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private BluetoothGattCharacteristic mUartTxCharacteristic;
    private BluetoothGattCharacteristic mUartRxCharacteristic;
    private Context context;
    private String recv = "";

    SmartDisc(Context context, String deviceName)
    {
        this.deviceName = deviceName;
        this.context = context;
        scan();
    }

    public void scan(){
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false).build();
        scanner.startScan(filters, settings, mScanCallback);
    }

    public void write(byte[] data){
        mUartTxCharacteristic.setWriteType(mUartTxCharacteristic.getWriteType());
        mUartTxCharacteristic.setValue(data);
        final boolean success = gattDevice.writeCharacteristic(mUartTxCharacteristic);
    }

    private List<DiscusPosListener> discusPosListeners = new ArrayList<DiscusPosListener>();
    public void addDataListener(DiscusPosListener discusPosListener){
        discusPosListeners.add(discusPosListener);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult result : results){
                if(result.getDevice().getName()!=null) {
                    if (result.getDevice().getName().equals(deviceName)) {
                        Log.d("scan", "device found");
                        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                        scanner.stopScan(mScanCallback);
                        result.getDevice().connectGatt(context, false, gattCallback);
                    }
                }
            }

        }
    };

    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gattDevice = gatt;
                        gatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        BluetoothGattService service = gatt.getService(kUartServiceUUID);
                        if(service!=null) {
                            mUartTxCharacteristic = service.getCharacteristic(kUartTxCharacteristicUUID);
                            mUartRxCharacteristic = service.getCharacteristic(kUartRxCharacteristicUUID);
                            if (mUartTxCharacteristic != null && mUartRxCharacteristic != null) {
                                Log.d("connect", "Uart Enable for: " + gatt.getDevice().getName());

                                BluetoothGattDescriptor descriptor = mUartRxCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                                if (gatt != null && descriptor != null && (mUartRxCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                    gatt.setCharacteristicNotification(mUartRxCharacteristic, true);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    final boolean success2 = gatt.writeDescriptor(descriptor);
                                    Log.d("sending data2", String.valueOf(success2));
                                    //characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                } else {
                                    Log.d("FAILED", "enable notify: client config descriptor not found for characteristic: " + mUartRxCharacteristic.getUuid().toString());
                                }
                            }
                        }
                    }
                }
                public byte[] toByteArray(List<Byte> in) {
                    final int n = in.size();
                    byte ret[] = new byte[n];
                    for (int i = 0; i < n; i++) {
                        ret[i] = in.get(i);
                    }
                    return ret;
                }

                Byte length = 0;
                boolean packetSending = false;
                int dataLength = 21;
                int currentIndex = 0;
                List<Byte> fullPacket = new ArrayList<Byte>();
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                    byte[] data = characteristic.getValue();
//                    Log.d("data", Arrays.toString(data));
                    for(byte val : data) {
                        fullPacket.add(val);
                        if(fullPacket.size()==dataLength){
                            int timeStamp = ByteBuffer.wrap(toByteArray(fullPacket.subList(0,4))).order(ByteOrder.LITTLE_ENDIAN).getInt();
                            int cmdType = fullPacket.get(4)&0xff;
                            float[] cmdData = new float[4];
                            cmdData[0] = ByteBuffer.wrap(toByteArray(fullPacket.subList(5,9))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            cmdData[1] = ByteBuffer.wrap(toByteArray(fullPacket.subList(9,13))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            cmdData[2] = ByteBuffer.wrap(toByteArray(fullPacket.subList(13,17))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            cmdData[3] = ByteBuffer.wrap(toByteArray(fullPacket.subList(17,21))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                            Log.d("data", String.valueOf(timeStamp));
                            for(DiscusPosListener listener : discusPosListeners){
                                    listener.newData(timeStamp, cmdType, cmdData);
                            }
                            fullPacket = new ArrayList<Byte>();
                        }
                    }
//                    for(byte val : data) {
//                        currentIndex += 1;
//                        if(packetSending){
//                            fullPacket.add(val);
//                            if(fullPacket.size()==dataLength) {
//                                float rotationReal = ByteBuffer.wrap(toByteArray(fullPacket.subList(0,4))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float rotationI = ByteBuffer.wrap(toByteArray(fullPacket.subList(4,8))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float rotationJ = ByteBuffer.wrap(toByteArray(fullPacket.subList(8,12))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float rotationK = ByteBuffer.wrap(toByteArray(fullPacket.subList(12,16))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//
//                                float linearAccX = ByteBuffer.wrap(toByteArray(fullPacket.subList(16,20))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float linearAccY = ByteBuffer.wrap(toByteArray(fullPacket.subList(20,24))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float linearAccZ = ByteBuffer.wrap(toByteArray(fullPacket.subList(24,28))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//
//                                float gyroscopeX = ByteBuffer.wrap(toByteArray(fullPacket.subList(28,32))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float gyroscopeY = ByteBuffer.wrap(toByteArray(fullPacket.subList(32,36))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float gyroscopeZ = ByteBuffer.wrap(toByteArray(fullPacket.subList(36,40))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//
//                                float latitude = ByteBuffer.wrap(toByteArray(fullPacket.subList(40,44))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float longitude = ByteBuffer.wrap(toByteArray(fullPacket.subList(44,48))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                                float altitude = ByteBuffer.wrap(toByteArray(fullPacket.subList(48,52))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//
//                                float timeStamp = ByteBuffer.wrap(toByteArray(fullPacket.subList(52,56))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//
//                                PositionData discPos = new PositionData(rotationReal, rotationI, rotationJ, rotationK, linearAccX, linearAccY, linearAccZ, gyroscopeX, gyroscopeY, gyroscopeZ, latitude, longitude, altitude, timeStamp);
//
//                                packetSending = false;
//                                fullPacket = new ArrayList<Byte>();
//                                currentIndex = 0;
//                                dataLength = 0;
//                            }
//                        }else{
//                            if(currentIndex==1) {
//                                if((val&0xff)==170) {
//
//                                }else {
//                                    currentIndex = 0;
//                                }
//                            }else if(currentIndex==2) {
//                                dataLength = val&0xff;
//                                packetSending = true;
//                            }
//
//                        }
//                    }

                }
            };

}
