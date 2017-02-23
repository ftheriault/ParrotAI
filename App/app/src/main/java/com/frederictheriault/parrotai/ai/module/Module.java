package com.frederictheriault.parrotai.ai.module;

import com.frederictheriault.parrotai.ai.DroneState;

public abstract class Module {

    public final void process(DroneState droneState) {
        processModule(droneState);
    }

    protected abstract void processModule(DroneState droneState);
}
