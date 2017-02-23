package com.frederictheriault.parrotai.ai;

import android.graphics.Bitmap;

import com.frederictheriault.parrotai.ai.module.Module;
import com.frederictheriault.parrotai.drone.JSDrone;

import java.util.ArrayList;
import java.util.List;

public abstract class DroneAI extends Thread {
    private final static int DELAY = 50;
    private boolean running;

    protected JSDrone drone;

    private List<Module> loadedModules;
    private DroneState droneState;

    public DroneAI(JSDrone drone) {
        this.drone = drone;
        loadedModules = new ArrayList<Module>();
        droneState = new DroneState();
    }

    public void addModule(Module module) {
        loadedModules.add(module);
    }

    public DroneState getDroneState() {
        return droneState;
    }

    public void stopRunning() {
        running = false;
    }

    @Override
    public void run(){
        running = true;

        while (running) {
            for (Module module : loadedModules) {
                module.process(droneState);
            }

            process();

            try {
                Thread.sleep(DELAY);
            }
            catch (InterruptedException ex) {
            }
        }
    }

    protected abstract void process();
}
