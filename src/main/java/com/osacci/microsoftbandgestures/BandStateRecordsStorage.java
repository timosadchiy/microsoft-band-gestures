package com.osacci.microsoftbandgestures;

import android.util.Log;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;

/**
 * Created by b0915218 on 10/06/16.
 */
public class BandStateRecordsStorage {

    private List<BandStateRecord> records = new ArrayList<BandStateRecord>();

    public BandStateRecordsStorage() {
        recordCleaner();
    }

    private void recordCleaner() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    List<BandStateRecord> toRemove = new ArrayList<BandStateRecord>();
                    try {
                        for (BandStateRecord record : records) {
                            if (record.getExpired()) {
                                toRemove.add(record);
                            } else {
                                break;
                            }
                        }
                    } catch (ConcurrentModificationException e) {
                        Log.e("BandStateRecordsStorage", e.toString());
                    }
                    for (BandStateRecord record : toRemove) {
                        records.remove(record);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void addRecord(BandStateRecord record) {
        records.add(record);
    }

    public List<BandStateRecord> getForPeriod(long period) {
        Date d = new Date(new Date().getTime() - period);
        List<BandStateRecord> result = new ArrayList<BandStateRecord>();
        for (BandStateRecord record : records) {
            if (record.dateCreated.before(d)) {
                continue;
            }
            try {
                BandStateRecord r = (BandStateRecord) record.clone();
                result.add(r);
            } catch (CloneNotSupportedException e) {

            }
        }
        return result;
    }

}
