package com.frederictheriault.parrotai.ai.module;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.frederictheriault.parrotai.ai.DroneState;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MovingTargetModule extends Module {
    private Bitmap previousBmp;
    private Point target;

    protected void processModule(DroneState droneState) {
        target = null;
        Bitmap bmp = droneState.getBitmap();

        if (previousBmp != null && bmp != null) {
            Bitmap tmp = calculateDiff(bmp, previousBmp);

            List<Point> shape = findShape(tmp);
            Log.i("MovingTargetModule", shape.size() + "");

            if (shape.size() > 0) {
                Integer[] border = findBorder(shape);

                int x = border[0] + border[2] / 2;
                int y = border[1] + border[3] / 2;

                target = new Point(x, y);
            }
            else {
                target = null;
            }

            droneState.setDisplayBitmap(tmp);
        }


        previousBmp = bmp;
    }

    public Point getTarget() {
        return target;
    }
    public Point getTargetPercent() {
        Point p = null;

        if (target != null && previousBmp != null) {
            int x = (int)((1.0 * target.x/previousBmp.getWidth()) * 100);
            int y = (int)((1.0 * target.y/previousBmp.getHeight()) * 100);

            p = new Point(x, y);
        }

        return p;
    }

    private boolean isInside(HashMap<String, Point> lines, HashMap<String, Point> cols, int x, int y) {
        boolean insideX = false;
        boolean insideY = false;

        Point line = lines.get(y + "");
        Point col = cols.get(x + "c");

        if (line != null) {
            if (line.x <= x && line.y >= x) {
                insideX = true;
            }
        }

        if (col != null) {

            if (col.x <= y && col.y >= y) {
                insideY = true;
            }
        }

        return  insideX && insideY;
    }

    private Integer[] findBorder(List<Point> shape) {
        int xMin = 10000;
        int xMax = 0;
        int yMin = 10000;
        int yMax = 0;

        for (int i = 0; i < shape.size(); i++) {
            if (shape.get(i).x < xMin)	xMin = shape.get(i).x;
            if (shape.get(i).x > xMax)	xMax = shape.get(i).x;
            if (shape.get(i).y < yMin)	yMin = shape.get(i).y;
            if (shape.get(i).y > yMax)	yMax = shape.get(i).y;
        }

        return new Integer[]{xMin, yMin, xMax - xMin, yMax - yMin};
    }

    private int colorSum(int pixel) {
        return Color.red(pixel) + Color.blue(pixel) + Color.green(pixel);
    }

    private Bitmap calculateDiff(Bitmap image1, Bitmap image2) {
        Bitmap imageData = Bitmap.createBitmap(image1.getWidth(), image1.getHeight(), image1.getConfig());
        int threshold = 140;
        int skip = 8;

        for (int x = skip/2; x < image1.getWidth() - skip; x+=skip) {
            for (int y = skip/2; y < image1.getHeight() - skip; y+=skip) {
                if (Math.abs(colorSum(image1.getPixel(x, y)) - colorSum(image2.getPixel(x, y))) > threshold) {
                    for (int x2 = x - skip/2; x2 < x + skip/2 && x2 < image1.getWidth(); x2++) {
                        for (int y2 = y - skip/2; y2 < y + skip/2 && y2 < image1.getHeight(); y2++){
                            imageData.setPixel(x2, y2, Color.BLACK);
                        }
                    }
                } else {
                    //imageData.setPixel(x, y, Color.WHITE);
                }
            }
        }

        return imageData;
    }

    private List<Point> findShape(Bitmap imageData) {
        int foundMin = -1;
        int foundMax = -1;
        int skip = 8;

        HashMap<String, Point> lines = new HashMap<>();

        // Scan horizontally
        for (int y = 1; y < imageData.getHeight(); y++) {
            foundMin = -1;
            foundMax = -1;

            for (int x = 1; x < imageData.getWidth() - skip; x+=skip) {
                if (foundMin == -1 && imageData.getPixel(x, y) == Color.BLACK) {
                    foundMin = x;
                }

                if (foundMax == -1 && imageData.getPixel(imageData.getWidth() - x, y) == Color.BLACK) {
                    foundMax = imageData.getWidth() - x;
                }

                if (foundMin != -1 && foundMax != -1) {
                    lines.put(y + "", new Point(foundMin, foundMax));
                    break;
                }
            }
        }

        HashMap<String, Point> cols = new HashMap<>();

        // Scan vertically
        for (int x = 1; x < imageData.getWidth(); x++) {
            foundMin = -1;
            foundMax = -1;

            for (int y = 1; y < imageData.getHeight() - skip; y+=skip) {
                if (foundMin == -1 && imageData.getPixel(x, y) == Color.BLACK) {
                    foundMin = y;
                }

                if (foundMax == -1 && imageData.getPixel(x, imageData.getHeight() - y) == Color.BLACK) {
                    foundMax = imageData.getHeight() - y;
                }

                if (foundMin != -1 && foundMax != -1) {
                    cols.put(x + "c", new Point(foundMin, foundMax));
                    break;
                }
            }
        }

        List<Point> shape = new ArrayList<>();
        int surroundCheck = 1;

        for (int x = 0; x < imageData.getWidth() - skip; x+=skip) {
            for (int y = 0; y < imageData.getHeight() - skip; y+=skip) {
                boolean valid = true;

                for (int z1 = -surroundCheck; z1 <= surroundCheck; z1++) {
                    for (int z2 = -surroundCheck; z2 <= surroundCheck; z2++) {
                        if (!isInside(lines, cols, x + z1, y + z2)) {
                            valid = false;
                            break;
                        }
                    }
                }

                if (valid) {
                    shape.add(new Point(x, y));
                }
            }
        }

        return shape;
    }
}
