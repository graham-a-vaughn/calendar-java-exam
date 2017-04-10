package gvaughn.example.calendar.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A CalendarEvent.
 */
@Entity
@Table(name = "calendar_event")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CalendarEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @NotNull
    @Column(name = "jhi_time", nullable = false)
    private ZonedDateTime time;

    @Size(max = 255)
    @Column(name = "location", length = 255)
    private String location;

    @NotNull
    @Column(name = "reminder_time", nullable = false)
    private ZonedDateTime reminderTime;

    @NotNull
    @Column(name = "reminder_sent", nullable = false)
    private Boolean reminderSent;

    @ElementCollection
    @CollectionTable(name = "calendar_event_attendees", joinColumns = @JoinColumn(name = "calendar_event_id"))
    @Column(name = "attendee_email")
    private Set<String> attendees = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    private Calendar calendar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public CalendarEvent title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public CalendarEvent time(ZonedDateTime time) {
        this.time = time;
        return this;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public CalendarEvent location(String location) {
        this.location = location;
        return this;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ZonedDateTime getReminderTime() {
        return reminderTime;
    }

    public CalendarEvent reminderTime(ZonedDateTime reminderTime) {
        this.reminderTime = reminderTime;
        return this;
    }

    public void setReminderTime(ZonedDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Boolean isReminderSent() {
        return reminderSent;
    }

    public CalendarEvent reminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
        return this;
    }

    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public Set<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(Set<String> attendees) {
        this.attendees = attendees;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public CalendarEvent calendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarEvent calendarEvent = (CalendarEvent) o;
        if (calendarEvent.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, calendarEvent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", time='" + time + "'" +
            ", location='" + location + "'" +
            ", reminderTime='" + reminderTime + "'" +
            ", reminderSent='" + reminderSent + "'" +
            '}';
    }
}
