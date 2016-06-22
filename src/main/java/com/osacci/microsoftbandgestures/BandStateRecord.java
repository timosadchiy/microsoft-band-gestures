package com.osacci.microsoftbandgestures;

import com.microsoft.band.sensors.BandGyroscopeEvent;

import java.util.Date;

public class BandStateRecord implements Cloneable {
    private static final long EXPIRES_AFTER = 5000;

    private ThreeDimensional acceleration;
    private ThreeDimensional angularVelocity;
    public final Date dateCreated;

    public BandStateRecord(BandGyroscopeEvent event) {
        this.dateCreated = new Date();
        this.acceleration = new ThreeDimensional();
        this.angularVelocity = new ThreeDimensional();
        this.acceleration.set(event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ());
        this.angularVelocity.set(event.getAngularVelocityX(), event.getAngularVelocityY(), event.getAngularVelocityZ());
    }

    public float getAccelerationX() {
        return acceleration.getX();
    }

    public float getAccelerationY() {
        return angularVelocity.getY();
    }

    public float getAccelerationZ() {
        return angularVelocity.getZ();
    }

    public float getAngularVelocityX() {
        return angularVelocity.getX();
    }

    public float getAngularVelocityY() {
        return angularVelocity.getY();
    }

    public float getAngularVelocityZ() {
        return angularVelocity.getZ();
    }

    public Boolean getExpired() {
        Date n = new Date();
        return n.getTime() - this.dateCreated.getTime() > EXPIRES_AFTER;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        BandStateRecord cloned = (BandStateRecord) super.clone();
        cloned.acceleration = (ThreeDimensional) cloned.acceleration.clone();
        cloned.angularVelocity = (ThreeDimensional) cloned.angularVelocity.clone();
        return cloned;
    }

}
