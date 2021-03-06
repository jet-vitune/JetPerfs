package com.jetsynthesys.jetanalytics;


import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.lang.ref.WeakReference;

abstract class JetxUploadTimer {
    /**
     * Millis since epoch when alarm should stop.
     */
    private final long mMillisInFuture;
    //private final WeakReference<JetxUploadTimer> mUploadTimerWeakReference;

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mCountdownInterval;

    private long mStopTimeInFuture;

    private boolean mCancelled = false;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    protected JetxUploadTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
        //mUploadTimerWeakReference = new WeakReference<>(JetxUploadTimer.this);
    }

    /**
     * Cancel the countdown.
     * <p>
     * Do not call it from inside CountDownTimer threads
     */
    public final void cancel() {
        // mHandler.removeCallbacks();
        mHandler.removeMessages(MSG);
        mCancelled = true;
    }

    /**
     * Start the countdown.
     */
    public synchronized final JetxUploadTimer start() {
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        mCancelled = false;
        return this;
    }


    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * Callback fired when the time is up.
     */
    public abstract void onFinish();

    private static final int MSG = 1;

    private static class UploadHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<JetxUploadTimer> uploadTimerWeakReference;
        JetxUploadTimer jetxUploadTimer;

        private UploadHandler(JetxUploadTimer myClassInstance) {
            uploadTimerWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            jetxUploadTimer = uploadTimerWeakReference.get();
            final long millisLeft = jetxUploadTimer.mStopTimeInFuture - SystemClock.elapsedRealtime();
            if (millisLeft <= 0) {
                jetxUploadTimer.onFinish();
            } else if (millisLeft < jetxUploadTimer.mCountdownInterval) {
                // no tick, just delay until done
                sendMessageDelayed(obtainMessage(MSG), millisLeft);
            } else {
                long lastTickStart = SystemClock.elapsedRealtime();
                jetxUploadTimer.onTick(millisLeft);
                // take into account user's onTick taking time to execute
                long delay = lastTickStart + jetxUploadTimer.mCountdownInterval - SystemClock.elapsedRealtime();
                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay < 0) delay += jetxUploadTimer.mCountdownInterval;

                if (!jetxUploadTimer.mCancelled) {
                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    }
    private UploadHandler mHandler = new UploadHandler(JetxUploadTimer.this);
}
