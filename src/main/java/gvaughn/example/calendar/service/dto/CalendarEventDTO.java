package gvaughn.example.calendar.service.dto;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO for calendar events.
 * Created by Graham Vaughn on 4/9/2017.
 */
public class CalendarEventDTO implements Serializable {

    private Long id;
    @Size(min = 1, max = 255)
    @NotNull
    private String title;
    @NotNull
    private ZonedDateTime time;
    private String location;
    @NotNull
    private ZonedDateTime reminderTime;
    private Set<String> attendees = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ZonedDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(ZonedDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Set<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(Set<String> attendees) {
        this.attendees = attendees;
    }

    @Override
    public String toString() {
        return "CalendarEventDTO{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", time=" + time +
            ", location='" + location + '\'' +
            ", reminderTime=" + reminderTime +
            ", attendees=" + attendees +
            '}';
    }
}
