package com.frederictheriault.parrotai.ai;

import android.graphics.Bitmap;

public class DroneState {
    private Bitmap displayBitmap;
    private Bitmap bmp;

    private long lastStateChanged;
    private boolean stateChanged;
    private int turnSpeed;
    private int movSpeed;
    private double rotate;

    public DroneState() {
    }

    public Bitmap getDisplayBitmap() {
        return displayBitmap;
    }

    public void setDisplayBitmap(Bitmap bitmap) {
        synchronized (this) {
            this.displayBitmap = bitmap;
        }
    }

    public Bitmap getBitmap() {
        Bitmap newBmp = null;

        if (bmp != null) {
            synchronized (this) {
                newBmp = bmp.copy(bmp.getConfig(), true);
            }
        }

        return newBmp;
    }

    public void setBitmap(Bitmap bmp) {
        synchronized (this) {
            this.bmp = bmp;
        }
    }

    public boolean isMoving() {
        return this.turnSpeed != 0 || this.movSpeed != 0 || this.rotate != 0.0;
    }

    public boolean processDone() {
        boolean changed = stateChanged;

        stateChanged = false;

        return changed;
    }

    private void stateChanged() {
        stateChanged = true;
        lastStateChanged = System.currentTimeMillis();
    }

    public long getLastStateChanged() {
        return lastStateChanged;
    }

    // Slowing down by ~10%
    public void slowDown() {
        stateChanged();

        this.movSpeed = (int)(this.movSpeed * 0.9);
        this.turnSpeed = (int)(this.turnSpeed * 0.9);
    }

    public void stop() {
        stateChanged();
        this.movSpeed = 0;
        this.turnSpeed = 0;
        this.rotate = 0;
    }

    public void incMovSpeed(int speed) {
        stateChanged();
        this.movSpeed += speed;
    }

    public int getMovSpeed() {
        return movSpeed;
    }

    public void setMovSpeed(int speed) {
        stateChanged();
        this.movSpeed = speed;
    }

    public void setTurnSpeed(int speed) {
        stateChanged();
        this.turnSpeed = speed;
    }

    public void incTurnSpeed(int speed) {
        stateChanged();
        this.turnSpeed += speed;
    }

    public int getTurnSpeed() {
        return turnSpeed;
    }

    public double getRotate() {
        return rotate;
    }

    public void setRotate(double rotate) {
        stateChanged();
        this.rotate = rotate;
    }

    public void setPilot(int speed, int turn) {
        stateChanged();
        this.movSpeed = speed;
        this.turnSpeed = turn;
    }
}
