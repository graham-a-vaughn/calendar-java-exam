package gvaughn.example.calendar.service;

import gvaughn.example.calendar.domain.CalendarEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Component class for managing calendar event reminders.
 *
 * Created by Graham Vaughn on 4/11/2017.
 */
@Component
public class CalendarEventReminderService {

    private static final Logger log = LoggerFactory.getLogger(CalendarEventReminderService.class);

    private static final long DEFAULT_REMINDER_MINUTES = 10;

    private static final Map<Long, ScheduledFuture> EXECUTION_MAP = Collections.synchronizedMap(new HashMap<>());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public void scheduleReminder(CalendarEvent calendarEvent) {
        Pair<Long, TimeUnit> delay = getReminderDelay(calendarEvent);
        if (delay != null) {
            ScheduledFuture future = scheduler.schedule(new ScheduledReminder(calendarEvent),
                delay.getLeft(), delay.getRight());
            EXECUTION_MAP.put(calendarEvent.getId(), future);
        }
    }

    public void updateReminder(CalendarEvent calendarEvent) {
        cancelReminder(calendarEvent.getId());
        scheduleReminder(calendarEvent);
    }

    public void cancelReminder(Long eventId) {
        if (EXECUTION_MAP.containsKey(eventId)) {
            ScheduledFuture future = EXECUTION_MAP.get(eventId);
            future.cancel(false);
            removeRemider(eventId);
        }
    }

    protected void removeRemider(Long eventId) {
        if (EXECUTION_MAP.containsKey(eventId)) {
            EXECUTION_MAP.remove(eventId);
        }
    }
    class ScheduledReminder implements Runnable {

        private final CalendarEvent calendarEvent;
        private final String reminderMessage;
        private final Long eventId;
        public ScheduledReminder(CalendarEvent calendarEvent) {
            this.calendarEvent = calendarEvent;
            this.reminderMessage = getReminderString(calendarEvent);
            this.eventId = calendarEvent.getId();
        }

        @Override
        public void run() {
            log.info("***** Calendar Event Reminder *****");
            log.info(reminderMessage);
            calendarEvent.setReminderSent(Boolean.TRUE);
            removeRemider(eventId);
        }
    }

    protected String getReminderString(CalendarEvent calendarEvent) {
        StringBuilder builder = new StringBuilder();
        builder.append("Don't forget you're scheduled event!").append(IOUtils.LINE_SEPARATOR)
            .append(calendarEvent.getTitle()).append(IOUtils.LINE_SEPARATOR)
            .append("Event Time: ").append(calendarEvent.getTime().format(DateTimeFormatter.RFC_1123_DATE_TIME))
            .append(IOUtils.LINE_SEPARATOR)
            .append("Location: ").append(Optional.of(calendarEvent.getLocation()).orElse("< no location provided >"))
            .append(IOUtils.LINE_SEPARATOR);
        return builder.toString();
    }

    protected Pair<Long, TimeUnit> getReminderDelay(CalendarEvent calendarEvent) {
        ZonedDateTime reminderTime = calendarEvent.getReminderTime();
        ZonedDateTime eventTime = calendarEvent.getTime();
        if (eventTime == null) {
            log.error("This event has no time. It is a non-event, and needs no reminder: " + calendarEvent);
            return null;
        }
        ZonedDateTime now = ZonedDateTime.now(reminderTime.getZone());
        if (eventTime.isAfter(now)) {
            log.error("This event is dust in the wind, no reminder necessary: " + calendarEvent);
            return null;
        }
        if (reminderTime == null) {
            log.warn("No reminder time specified, using default reminder.");
            return getDefaultReminderDelay(eventTime, now);
        }
        if (!reminderTime.isAfter(now)) {
            log.warn("Requested reminder time is in the past, using default reminder");
            return getDefaultReminderDelay(eventTime, now);
        }
        if (!reminderTime.isBefore(eventTime)) {
            log.warn("Requested reminder time is not before the event, using default reminder");
            return getDefaultReminderDelay(eventTime, now);
        }
        long delayInMinutes = now.until(reminderTime, ChronoUnit.MINUTES);
        return Pair.of(delayInMinutes, TimeUnit.MINUTES);
    }

    protected Pair<Long, TimeUnit> getDefaultReminderDelay(ZonedDateTime eventTime, ZonedDateTime now) {
        ZonedDateTime reminderTime = eventTime.minusMinutes(DEFAULT_REMINDER_MINUTES);
        if (!reminderTime.isAfter(now)) {
            return Pair.of(10L, TimeUnit.SECONDS);
        }
        long delayInMinutes = now.until(reminderTime, ChronoUnit.MINUTES);
        return Pair.of(delayInMinutes, TimeUnit.MINUTES);
    }
}
