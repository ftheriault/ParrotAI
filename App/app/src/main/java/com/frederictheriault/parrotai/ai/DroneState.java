package com.frederictheriault.parrotai.ai;

import android.graphics.Bitmap;

public class DroneState {
    private Bitmap displayBitmap;
    private Bitmap bmp;

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
}
