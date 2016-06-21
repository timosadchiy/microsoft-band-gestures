package com.timosadchiy.microsoftbandgestures;

import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.SampleRate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BGM implements BandGestureManager {
    private static final float SWIPE_MIN_ABS_VELOCITY = 270;
    private static final float SWIPE_MIN_ABS_Y = 0.8f;
    private static final long MIN_SWIPE_INTERVAL = 500;

    private static final long MAX_INTENTION_LOCK_DURATION = 500;
    private static final long MIN_INTENTION_LOCK_ANGLE = 80;
    private static final long MIN_INTENTION_LOCK_INTERVAL = 500;

    private Boolean leftHand;
    private Boolean outsideHand;

    private BandSensorManager bandSensorManager;
    private List<PalmSideSwipeEventListener> palmSideSwipeRightListeners = new ArrayList<PalmSideSwipeEventListener>();
    private List<PalmSideSwipeEventListener> palmSideSwipeLeftListeners = new ArrayList<PalmSideSwipeEventListener>();
    private List<IntentionLockEventListener> intentionLockEventListeners = new ArrayList<IntentionLockEventListener>();

    private Date swipeFiredOn = new Date();
    private Date intentionLockFiredOn = new Date();
    private boolean intentionLocked = false;
    private ExecutorService es = Executors.newSingleThreadExecutor();

    private BandStateRecordsStorage bandStateRecordsStorage;

    public BGM(BandSensorManager sensorManager) throws BandIOException {
        bandStateRecordsStorage = new BandStateRecordsStorage();
        bandSensorManager = sensorManager;
        bandSensorManager.registerGyroscopeEventListener(mGyroEventListener, SampleRate.MS128);
        leftHand = true;
        outsideHand = true;
    }

    private BandGyroscopeEventListener mGyroEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            if (event != null) {
                bandStateRecordsStorage.addRecord(new BandStateRecord(event));
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        checkForSwipe(event);
                        checkForIntentionLock();
                    }
                };
                es.submit(r);
            }
        }
    };

    private void checkForSwipe(BandGyroscopeEvent event) {
        float minSwipeVelocity = SWIPE_MIN_ABS_VELOCITY;
        float minSwipeY = SWIPE_MIN_ABS_Y;
        float accY = event.getAccelerationY();
        float avY = event.getAngularVelocityY();
        if (Math.abs(avY) < minSwipeVelocity || Math.abs(accY) < minSwipeY) {
            return;
        }
        if (swipeJustFired()) {
            return;
        }
        if (leftHand) {
            if (outsideHand) {
                if (accY < 0) {
                    return;
                }
                if (avY < 0) {
                    firePalmSideSwipeRight();
                } else {
                    firePalmSideSwipeLeft();
                }
            } else {
                if (accY > 0) {
                    return;
                }
                if (avY > 0) {
                    firePalmSideSwipeRight();
                } else {
                    firePalmSideSwipeLeft();
                }
            }
        } else {
            if (outsideHand) {
                if (accY > 0) {
                    return;
                }
                if (avY > 0) {
                    firePalmSideSwipeRight();
                } else {
                    firePalmSideSwipeLeft();
                }
            } else {
                if (accY < 0) {
                    return;
                }
                if (avY < 0) {
                    firePalmSideSwipeRight();
                } else {
                    firePalmSideSwipeLeft();
                }
            }
        }
    }

    private void checkForIntentionLock() {
        if (intentionLockJustFired()) {
            return;
        }
        List<BandStateRecord> records = bandStateRecordsStorage.getForPeriod(MAX_INTENTION_LOCK_DURATION);
        if (records.size() < 2) {
            return;
        }
        float angularDistance = 0;
        float angularOppositeDistance = 0;
        Float prevDistance = null;
        int directionSwitchCount = 0;
        for (int i = 1, l = records.size(); i < l; i++) {
            BandStateRecord prevRecord = records.get(i - 1);
            BandStateRecord curRecord = records.get(i);
            float travelTime = (curRecord.dateCreated.getTime() - prevRecord.dateCreated.getTime()) / 1000.0f;
            float distance = travelTime * curRecord.getAngularVelocityX();

            if (prevDistance == null) {
                prevDistance = distance;
            } else {
                if ((prevDistance < 0) ? (distance >= 0) : (distance < 0)) {
                    directionSwitchCount++;
                }
                prevDistance = distance;
            }

            if (Math.abs(angularDistance) < MIN_INTENTION_LOCK_ANGLE) {
                angularDistance += ((angularDistance >= 0) ^ (distance < 0)) ? distance : 0;
                if (Math.abs(angularDistance) >= MIN_INTENTION_LOCK_ANGLE) {
                    angularOppositeDistance += angularDistance >= 0 ? -0.001 : 0.001;
                }
            } else {
                angularOppositeDistance += (angularOppositeDistance >= 0) ^ (distance < 0) ? distance : 0;
            }
        }

        if (Math.abs(angularDistance) >= MIN_INTENTION_LOCK_ANGLE &&
                Math.abs(angularOppositeDistance) >= MIN_INTENTION_LOCK_ANGLE &&
                directionSwitchCount <= 2 &&
                ((angularDistance < 0) ? (angularOppositeDistance >= 0) : (angularOppositeDistance < 0))) {
            intentionLocked = !intentionLocked;
            ILEvent e = new ILEvent(intentionLocked);
            fireIntentionLock(e);
        }
    }

    public void setLeftHandOutside() {
        leftHand = true;
        outsideHand = true;
    }

    public void setLeftHandInside() {
        leftHand = true;
        outsideHand = false;
    }

    public void setRightHandOutside() {
        leftHand = false;
        outsideHand = true;
    }

    public void setRightHandInside() {
        leftHand = false;
        outsideHand = false;
    }

    public boolean registerPalmSideSwipeRightEventListener(PalmSideSwipeEventListener listener) throws BandIOException {
        Boolean r = palmSideSwipeRightListeners.add(listener);
        return r;
    }

    public void unregisterPalmSideSwipeRightEventListeners() throws BandIOException {
        palmSideSwipeRightListeners.clear();
    }

    public void unregisterPalmSideSwipeRightEventListener(PalmSideSwipeEventListener listener) throws BandIOException {
        palmSideSwipeRightListeners.remove(listener);
    }

    public boolean registerPalmSideSwipeLeftEventListener(PalmSideSwipeEventListener listener) throws BandIOException {
        Boolean r = palmSideSwipeLeftListeners.add(listener);
        return r;
    }

    public void unregisterPalmSideSwipeLeftEventListeners() throws BandIOException {
        palmSideSwipeLeftListeners.clear();
    }

    public void unregisterPalmSideSwipeLeftEventListener(PalmSideSwipeEventListener listener) throws BandIOException {
        palmSideSwipeLeftListeners.remove(listener);
    }

    public boolean registerIntentionLockEventListener(IntentionLockEventListener listener) throws BandIOException {
        Boolean r = intentionLockEventListeners.add(listener);
        return r;
    }

    public void unregisterIntentionLockEventListeners() throws BandIOException {
        intentionLockEventListeners.clear();
    }

    public void unregisterIntentionLockEventListener(IntentionLockEventListener listener) throws BandIOException {
        intentionLockEventListeners.remove(listener);
    }

    private void firePalmSideSwipeRight() {
        swipeFiredOn = new Date();
        for (PalmSideSwipeEventListener e : palmSideSwipeRightListeners) {
            e.onSwipe();
        }
    }

    private void firePalmSideSwipeLeft() {
        swipeFiredOn = new Date();
        for (PalmSideSwipeEventListener e : palmSideSwipeLeftListeners) {
            e.onSwipe();
        }
    }

    private void fireIntentionLock(ILEvent event) {
        intentionLockFiredOn = new Date();
        for (IntentionLockEventListener e : intentionLockEventListeners) {
            e.onChange(event);
        }
    }

    private Boolean swipeJustFired() {
        Date now = new Date();
        long d = now.getTime() - swipeFiredOn.getTime();
        return d < MIN_SWIPE_INTERVAL;
    }

    private Boolean intentionLockJustFired() {
        Date now = new Date();
        long d = now.getTime() - intentionLockFiredOn.getTime();
        return d < MIN_INTENTION_LOCK_INTERVAL;
    }

    private class ILEvent implements IntentionLockEvent {
        private final boolean locked;

        public ILEvent(boolean b) {
            locked = b;
        }

        public boolean getLocked() {
            return locked;
        }

    }

}
