package com.example.discustrainer;

import java.util.ArrayList;

public class DiscusEventData {
    private long timestamp = 0;
    private int eventType = 0;
    private ArrayList<Float> data = new ArrayList<>();
    public DiscusEventData(long timestamp, int eventType, float dataValOne, float dataValTwo, float dataValThree, float dataValFour){
        this.timestamp = timestamp;
        this.eventType = eventType;
        data.add(dataValOne);
        data.add(dataValTwo);
        data.add(dataValThree);
        data.add(dataValFour);
    }

    public long getTimestamp(){
        return timestamp;
    }

    public int getEventType(){
        return eventType;
    }

    public ArrayList<Float> getData(){
        return data;
    }
}
