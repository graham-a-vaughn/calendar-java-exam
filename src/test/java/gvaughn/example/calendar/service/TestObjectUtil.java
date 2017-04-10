package gvaughn.example.calendar.service;

import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.domain.User;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * Utility class for creating test objects.
 *
 * Created by Graham Vaughn on 4/10/2017.
 */
public class TestObjectUtil {

    public static final String EVENT_TITLE = "Important Event";

    public static final ZonedDateTime DEFAULT_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    public static final String DEFAULT_LOCATION = "Lincoln, Nebraska";
    public static final String[] DEFAULT_ATTENDEES = new String[]{"john.doe@example.com", "doe.re@example.com"};

    public static final ZonedDateTime DEFAULT_REMINDER_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);

    private static final Boolean DEFAULT_REMINDER_SENT = false;

    public static User createUser(UserService userService) {
        return createUser(userService, Math.round(Math.random()));
    }

    public static User createUser(UserService userService, long index) {
        return userService.createUser(index + "user@example.com", "password", "User", "Bruiser",
            index + "user@example.com", null, null);
    }

    public static CalendarEvent createCalendarEvent() {
        CalendarEvent calendarEvent = new CalendarEvent()
            .title(EVENT_TITLE)
            .time(DEFAULT_TIME)
            .location(DEFAULT_LOCATION)
            .reminderTime(DEFAULT_REMINDER_TIME)
            .reminderSent(DEFAULT_REMINDER_SENT);
        calendarEvent.getAttendees().addAll(Arrays.asList(DEFAULT_ATTENDEES));
        return calendarEvent;
    }

    public static boolean newEventMatchesSource(CalendarEvent source, CalendarEvent result) {
        return EqualsBuilder.reflectionEquals(source, result, "id", "calendar");
    }
}
