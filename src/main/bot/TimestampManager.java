package main.bot;

public class TimestampManager {

    private Long timestamp;

    public TimestampManager() {
        this.timestamp = 0L;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long offset(Long timestamp) {
        return timestamp - this.timestamp;
    }

    public void call(Long currentTimestamp, Long interval, TimestampCallback callback) {
        if (offset(currentTimestamp) >= interval) {
            callback.call();
            setTimestamp(currentTimestamp);
        }
    }

    public interface TimestampCallback {
        void call();
    }
}