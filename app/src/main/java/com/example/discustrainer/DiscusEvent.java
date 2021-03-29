package com.example.discustrainer;

import java.util.Hashtable;

public class DiscusEvent {
    public Hashtable linearAcceleration = new Hashtable();
    public  Hashtable gyroAcceleration = new Hashtable();
    public Hashtable Orientation = new Hashtable();
    public Hashtable gpsCoords = new Hashtable();

    void addLinearAccelerationEvent(long timeStamp, float[] data){
        linearAcceleration.put(timeStamp, data);
    }

    void addGyroAcceleration(long timeStamp, float[] data){
        gyroAcceleration.put(timeStamp, data);
    }

    void addOrientationEvent(long timeStamp, float[] data){
        Orientation.put(timeStamp, data);
    }

    void addGpsCoords(long timeStamp, float[] data){
        gpsCoords.put(timeStamp, data);
    }
}
