package main.BasicClasses;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    private float x;
    private long y;

    public Coordinates(float x, long y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public String toString() {
        return Float.toString(x) + " " + Long.toString(y);
    }
}
