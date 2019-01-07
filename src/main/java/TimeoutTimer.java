import java.util.Timer;
import java.util.TimerTask;

public class TimeoutTimer {

    private boolean isFinished;

    public TimeoutTimer(long timeoutInSeconds) {
        startTimer(timeoutInSeconds);
    }

    private void startTimer(long timeoutInSeconds) {
        isFinished = false;
        TimerTask task = new TimerTask() {
            public void run() {
                isFinished = true;
            }
        };
        Timer timer = new Timer("TimeoutTimer");
        timer.schedule(task, timeoutInSeconds * 1000);
    }

    public boolean isFinished() {
        return isFinished;
    }

}
