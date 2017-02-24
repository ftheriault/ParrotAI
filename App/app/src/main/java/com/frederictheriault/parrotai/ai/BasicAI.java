package com.frederictheriault.parrotai.ai;

import android.graphics.Point;
import android.util.Log;

import com.frederictheriault.parrotai.ai.module.MovingTargetModule;
import com.frederictheriault.parrotai.drone.JSDrone;

public class BasicAI extends DroneAI {
    private MovingTargetModule movingTargetModule;
    private boolean canFollow;
    private int movementDelay = 500;

    public BasicAI(JSDrone drone, boolean canFollow) {
        super(drone);
        movingTargetModule = new MovingTargetModule();
        this.canFollow = canFollow;

        super.addModule(movingTargetModule);

        drone.setHeadlightIntensity(0, 0);
    }

    protected void process() {
        if (getDroneState().getLastStateChanged() + movementDelay < System.currentTimeMillis()) {
            if (!droneState.isMoving()) {
                Point target = movingTargetModule.getTargetPercent();

                if (target != null) {
                    int dist = Math.abs(50 - target.x);

                    if (dist > 10) {
                        if (target.x < 50) {
                            droneState.setRotate(-Math.toRadians((dist/50.0) * 75)); // rotate on itself
                        } else if (target.x > 50) {
                            droneState.setRotate(Math.toRadians((dist/50.0) * 75)); // rotate on itself
                        }

                        if (this.canFollow) {
                            droneState.setMovSpeed(20);
                        }
                    }
                    else {
                        drone.setHeadlightIntensity(20, 20);
                    }

                    movementDelay = 1000;
                }
            } else {
                droneState.stop();
                movementDelay = 2000;
            }
        }
    }
}
