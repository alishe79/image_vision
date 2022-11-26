package com.sensifai.image_vision;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class Labels {
    Context context;
    public Labels(Context context){
        this.context = context;
    }

    String getLabelByIndex(int index){
        String label = "";
        try {
            InputStream is = context.getAssets().open("labels.txt");

            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            String text = new String(buffer);
            String[] labelsStringArray = text.split("\n");
            ArrayList<String> labels = new ArrayList(Arrays.asList(labelsStringArray));
            label = labels.get(index);

            // Finally stick the string into the text view.
            // Replace with whatever you need to have the text into.

        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
        return label;
    }


    String getDescriptionByLabel(String label){
        String description = "";
        try {
            InputStream is = context.getAssets().open("label_description.csv");

            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            String text = new String(buffer);
            String[] labelsStringArray = text.split(label + ",");
            description = labelsStringArray[1].split("\n")[0];

            // Finally stick the string into the text view.
            // Replace with whatever you need to have the text into.

        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
        return description;
    }
}
