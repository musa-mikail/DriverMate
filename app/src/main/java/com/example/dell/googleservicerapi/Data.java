package com.example.dell.googleservicerapi;

import android.util.Log;

/**
 * Created by DELL on 4/14/2018.
 */

public class Data {

    private int countHA, countHB;
    private float distance, maxSpeed,perform;
    private boolean isRunning=true;//remmeber to change this when you implememnt the start button
    private long time;

    public Data() {
        countHA=0;
        countHB=0;
        distance=0f;
        maxSpeed=0f;
        perform=100;
    }

    public int getCountHA() {
        return countHA;
    }

    public void setCountHA() {
        countHA++;
    }

    public int getCountHB() {
        return countHB;
    }

    public void setCountHB() {
        countHB++;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float mydistance) {

        this.distance = this.distance + mydistance;
        Log.w("From data","internal check");
    }


    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float mySpeed) {
        if (mySpeed > this.maxSpeed)
        this.maxSpeed = mySpeed;
    }


    public float getPerform() {
        perform = 100;

        if (!(distance==0)) {

            if (countHA * (1428 / (0.001f * distance)) > 50) {
                perform = perform - 50;
            } else {
                perform = perform - (countHA * (1428 / (0.001f * distance)));
            }

            if (countHB * (1428 / (0.001f * distance)) > 50) {
                perform = perform - 50;
            } else {
                perform = perform - (countHB * (1428 / (0.001f * distance)));
            }
        }
        if (perform<0) perform=0;

        return perform;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Data{" +
                "countHA=" + countHA +
                ", countHB=" + countHB +
                ", distance=" + distance +
                ", maxSpeed=" + maxSpeed +
                ", perform=" + perform +
                '}';
    }
}



