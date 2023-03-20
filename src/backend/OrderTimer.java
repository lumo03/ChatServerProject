package backend;

import backend.types.OrderTimerFn;

public class OrderTimer {
    private final int seconds;

    private final int milliseconds;

    private final int TIME_UPDATE_INTERVAL_SEC;

    private final int TIME_UPDATE_INTERVAL_MILLIS;

    private final OrderTimerFn callback;

    public OrderTimer(int seconds, int updateIntervalInSeconds, OrderTimerFn callback) {
        this.seconds = seconds;
        milliseconds = seconds * 1000;
        TIME_UPDATE_INTERVAL_SEC = updateIntervalInSeconds;
        TIME_UPDATE_INTERVAL_MILLIS = TIME_UPDATE_INTERVAL_SEC * 1000;
        this.callback = callback;
    }

    public void start() {
        int timeLeft = milliseconds;

        while (timeLeft > 0) {
            try {
                Thread.sleep(TIME_UPDATE_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            timeLeft -= TIME_UPDATE_INTERVAL_MILLIS;

            if (timeLeft < 0) {
                timeLeft = 0;
            }

            callback.run("Time left: " + timeLeft / 1000 + " seconds");
        }
    }
}
