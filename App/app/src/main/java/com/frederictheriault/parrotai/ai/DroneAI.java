package com.frederictheriault.parrotai.ai;

import android.graphics.Bitmap;
import android.util.Log;

import com.frederictheriault.parrotai.ai.module.Module;
import com.frederictheriault.parrotai.drone.JSDrone;

import java.util.ArrayList;
import java.util.List;

public abstract class DroneAI extends Thread {
    private final static int DELAY = 25;
    private boolean running;

    protected JSDrone drone;
    protected DroneState droneState;

    private List<Module> loadedModules;

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

            boolean stateChanged = droneState.processDone();

            if (stateChanged) {
                if (droneState.isMoving()) {
                    Log.i("BasicAI", "Moving:" + droneState.getMovSpeed() + "/" + droneState.getTurnSpeed());
                    drone.pilot((byte) droneState.getMovSpeed(), (byte) droneState.getTurnSpeed());

                    if (droneState.getRotate() != 0.0) {
                        drone.rotate(droneState.getRotate());
                        droneState.setRotate(0);
                    }
                }
                else {
                    Log.i("BasicAI", "Stop");
                    drone.stop();
                }
            }

            try {
                Thread.sleep(DELAY);
            }
            catch (InterruptedException ex) {
            }
        }

        drone.stop();
    }

    protected abstract void process();
}
