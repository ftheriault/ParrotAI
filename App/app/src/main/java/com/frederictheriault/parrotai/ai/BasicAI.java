package com.frederictheriault.parrotai.ai;

import android.util.Log;

public class BasicAI extends DroneAI {
    private int cycle = 0;
    public BasicAI() {

    }

    protected void process() {
        Log.i("BasicAI", cycle++ + "");
    }
}
