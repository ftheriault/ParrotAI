package com.frederictheriault.parrotai.ai;

public abstract class DroneAI extends Thread {
    private final static int DELAY = 50;
    private boolean running;

    public DroneAI() {

    }

    public void stopRunning() {
        running = false;
    }

    @Override
    public void run(){
        running = true;

        while (running) {
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
