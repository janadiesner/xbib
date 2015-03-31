package org.snmp4j.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This <code>DefaultTimerFactory</code> creates a new <code>Timer</code>
 * which is configured to run as daemon.
 */
public class DefaultTimerFactory implements TimerFactory {

    public DefaultTimerFactory() {
    }

    public CommonTimer createTimer() {
        return new TimerAdapter();
    }

    class TimerAdapter implements CommonTimer {

        private Timer timer = new Timer(true);

        public void schedule(TimerTask task, long delay) {
            timer.schedule(task, delay);
        }

        public void cancel() {
            timer.cancel();
        }

        public void schedule(TimerTask task, Date firstTime, long period) {
            timer.schedule(task, firstTime, period);
        }

        public void schedule(TimerTask task, long delay, long period) {
            timer.schedule(task, delay, period);
        }
    }
}
