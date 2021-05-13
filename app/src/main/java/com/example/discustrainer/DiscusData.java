package com.example.discustrainer;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;

public class DiscusData implements DiscusEventListener {
    public ArrayList<DiscusEventData> discusDataEvents = new ArrayList<>();
    @Override
    public void newData(DiscusEventData discusEvent) {
        discusDataEvents.add(discusEvent);
    }

    public boolean toCsv(Context context){
        String fileName = "GpsData.csv";
        Log.d("CSV Save", "Saving");
        CSVWriter writer;
        try {
            Writer mFileWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            writer = new CSVWriter(mFileWriter);
            for(DiscusEventData event : discusDataEvents){
                ArrayList<Float> data = event.getData();
                String[] eventLine = new String[2+data.size()];
                eventLine[0] = String.valueOf(event.getTimestamp());
                String eventName = "";
                switch(event.getEventType()) {
                    case 2:
                        eventName = "Gyro Acceleration";
                        break;
                    case 4:
                        eventName = "Linear Acceleration";
                        break;
                    case 8:
                        eventName = "Orientation";
                        break;
                    case 17:
                        eventName = "GPS";
                        break;
                }
                eventLine[1] = String.valueOf(eventName);
                int idx = 2;
                for(Float dataValue : data){
                    eventLine[idx] = String.valueOf(dataValue);
                    idx+=1;
                }
                writer.writeNext(eventLine);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
