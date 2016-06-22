package com.osacci.microsoftbandgestures;

public class ThreeDimensional implements Cloneable {
    private float x;
    private float y;
    private float z;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float v) {
        x = v;
    }

    public void setY(float v) {
        y = v;
    }

    public void setZ(float v) {
        z = v;
    }

    public void set(float v1, float v2, float v3) {
        x = v1;
        y = v2;
        z = v3;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ThreeDimensional cloned = (ThreeDimensional) super.clone();
        return cloned;
    }

}
