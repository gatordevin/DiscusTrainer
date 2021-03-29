package com.example.discustrainer;

public class PositionData {
    float rotationReal;
    float rotationI;
    float rotationJ;
    float rotationK;
    float linearAccX;
    float linearAccY;
    float linearAccZ;
    float gyroscopeX;
    float gyroscopeY;
    float gyroscopeZ;
    float latitude;
    float longitude;
    float altitude;
    float timeStamp;
    public PositionData(float rotationReal, float rotationI, float rotationJ, float rotationK, float linearAccX, float linearAccY, float linearAccZ, float gyroscopeX, float gyroscopeY, float gyroscopeZ, float latitude, float longitude, float altitude, float timeStamp){
        this.rotationReal = rotationReal;
        this.rotationI = rotationI;
        this.rotationJ = rotationJ;
        this.rotationK = rotationK;
        this.linearAccX = linearAccX;
        this.linearAccY = linearAccY;
        this.linearAccZ = linearAccZ;
        this.gyroscopeX = gyroscopeX;
        this.gyroscopeY = gyroscopeY;
        this.gyroscopeZ = gyroscopeZ;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timeStamp = timeStamp;
    }
}
