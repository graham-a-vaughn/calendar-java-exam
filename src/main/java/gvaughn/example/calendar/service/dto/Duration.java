package gvaughn.example.calendar.service.dto;

/**
 * Represents a duration of time, used as a search parameter.
 *
 * Created by Graham Vaughn on 4/10/2017.
 */
public enum Duration {

    DAY(1),

    WEEK(7),

    MONTH(30);

    private final int days;

    private Duration(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}
