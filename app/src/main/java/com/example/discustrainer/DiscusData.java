package com.example.discustrainer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

public class DiscusData implements DiscusPosListener{
    ArrayList<DiscusEvent> events = new ArrayList<DiscusEvent>();
    public Hashtable linearAcceleration = new Hashtable();
    public  Hashtable gyroAcceleration = new Hashtable();
    public Hashtable Orientation = new Hashtable();
    public Hashtable gpsCoords = new Hashtable();
    @Override
    public void newData(long timestamp, int cmdType, float[] data) {
        switch(cmdType) {
            case 2:
                gyroAcceleration.put(timestamp, data);
                break;
            case 4:
                linearAcceleration.put(timestamp, data);
                break;
            case 8:
                Orientation.put(timestamp, data);
                break;
            case 17:
                gpsCoords.put(timestamp, data);
                break;
        }
    }
    public DiscusEvent getLatest(){
        return events.get(events.size());
    }
}
