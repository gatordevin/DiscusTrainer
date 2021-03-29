package com.example.discustrainer;

public interface DiscusPosListener {
    public void newData(long timestamp, int cmdType, float[] data);
}
