package net.zyc.ss.udptransapp;


import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

/**
 * Created by 龚志星 on 2017/12/15 at 12:29
 */

public class MyHandler extends Handler {
    private final WeakReference<Activity> weakReference;

    public MyHandler(Callback callback, Activity activity) {
        super(callback);
        weakReference=new WeakReference<>(activity);
    }

    public MyHandler(Looper looper, Activity activity) {
        super(looper);
        weakReference=new WeakReference<>(activity);
    }

    public MyHandler(Callback callback) {
        super(callback);
        this.weakReference = new WeakReference<Activity>(null);
    }

    public WeakReference<Activity> getWeakReference() {
        return weakReference;
    }
}
