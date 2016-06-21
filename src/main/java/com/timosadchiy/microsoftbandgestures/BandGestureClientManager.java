package com.timosadchiy.microsoftbandgestures;

import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandSensorManager;

/**
 * Created by b0915218 on 07/06/16.
 */
public final class BandGestureClientManager {
    public static BandGestureManager getGestureManager(BandSensorManager sensorManager) throws BandIOException {
        return new BGM(sensorManager);
    };
}
