package com.frederictheriault.parrotai.ai;

import android.graphics.Point;
import android.util.Log;

import com.frederictheriault.parrotai.ai.module.MovingTargetModule;
import com.frederictheriault.parrotai.drone.JSDrone;

public class BasicAI extends DroneAI {
    private MovingTargetModule movingTargetModule;

    private int turnSpeed = 0;
    private int movSpeed = 0;

    private static final int MOVING_DELAY = 15;
    private int movingDelay;

    public BasicAI(JSDrone drone) {
        super(drone);
        movingTargetModule = new MovingTargetModule();

        super.addModule(movingTargetModule);
    }

    protected void process() {
        Point target = movingTargetModule.getTargetPercent();

        if (movingDelay > 0) {
            movingDelay--;
            drone.setFlag((byte) 0);

            if (movingDelay == 0) {
                turnSpeed = 0;
                drone.setTurn((byte) 0);
            }
        }
        else {
            int previousTurnSpeed = turnSpeed;

            if (target != null) {
                if (target.x < 45) {
                    turnSpeed = -2;
                } else if (target.x > 45) {
                    turnSpeed = 2;
                }

                //Log.i("BasicAI", target.x + "/" + target.y);
            }

            if (turnSpeed != previousTurnSpeed) {
                movingDelay = MOVING_DELAY;
                Log.i("BasicAI", turnSpeed + "/" + previousTurnSpeed);
                drone.setTurn((byte) turnSpeed);
                drone.setFlag((byte) 1);
            }
        }
    }
}
