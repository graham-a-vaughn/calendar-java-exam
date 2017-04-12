package gvaughn.example.calendar.service.dto;

import java.time.ZonedDateTime;

/**
 * Encapsulates a span of time, e.g. a period for which calendar events will be searched.
 *
 * Created by Graham Vaughn on 4/10/2017.
 */
public class EventListDurationDTO {

    private final Duration duration;
    private final ZonedDateTime startDate;

    public EventListDurationDTO(Duration duration, ZonedDateTime startDate) {
        this.duration = duration;
        this.startDate = startDate;
    }

    public Duration getDuration() {
        return duration;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }
}
