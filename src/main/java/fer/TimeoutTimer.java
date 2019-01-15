package fer;

import java.util.Timer;
import java.util.TimerTask;

public class TimeoutTimer {

    private boolean isFinished;

    public TimeoutTimer(long timeoutInSeconds, TimerTask repetitiveTask, long periodInSeconds) {
        startTimer(timeoutInSeconds);
        setRepetitiveTask(repetitiveTask, periodInSeconds);
    }

    public boolean isFinished() {
        return isFinished;
    }

    private void startTimer(long timeoutInSeconds) {
        isFinished = false;
        TimerTask task = new TimerTask() {
            public void run() {
                isFinished = true;
            }
        };
        Timer timer = new Timer("fer.TimeoutTimer");
        timer.schedule(task, timeoutInSeconds * 1000);
    }

    private void setRepetitiveTask(TimerTask task, long periodInSeconds) {
        Timer timer = new Timer("RepetitiveTimer");
        timer.scheduleAtFixedRate(task, periodInSeconds * 1000, periodInSeconds * 1000);
    }


}
