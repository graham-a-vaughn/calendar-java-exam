package gvaughn.example.calendar.service;

import gvaughn.example.calendar.CalendarApp;
import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.domain.User;
import gvaughn.example.calendar.repository.CalendarEventRepository;
import gvaughn.example.calendar.service.dto.CalendarEventDTO;
import gvaughn.example.calendar.service.dto.Duration;
import gvaughn.example.calendar.service.dto.EventListDurationDTO;
import gvaughn.example.calendar.service.mapper.CalendarEventMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Created by Graham Vaughn on 4/10/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CalendarApp.class)
@Transactional
public class CalendarEventServiceIntTest {

    private static final int USER_EVENT_COUNT = 3;

    @Autowired
    private UserService userService;

    @Autowired
    private CalendarEventService calendarEventService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarEventMapper calendarEventMapper;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Test
    public void validEventIsCreated() {
        User user = createUser();
        CalendarEvent event = TestObjectUtil.createCalendarEvent();
        CalendarEventDTO dto = calendarEventMapper.calendarEventToDTO(event);
        CalendarEvent newEvent = calendarEventService.create(dto, user);
        assertNotNull(newEvent);
        assertTrue(TestObjectUtil.newEventMatchesSource(event, newEvent));
    }

    @Test(expected = NullPointerException.class)
    public void eventDTOIsRequired() {
        User user = createUser();
        CalendarEvent newEvent = calendarEventService.create(null, user);
    }

    @Test(expected = NullPointerException.class)
    public void eventUserIsRequired() {
        CalendarEvent event = TestObjectUtil.createCalendarEvent();
        CalendarEventDTO dto = calendarEventMapper.calendarEventToDTO(event);
        CalendarEvent newEvent = calendarEventService.create(dto, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newEventIsRequiredForCreate() {
        User user = createUser();
        CalendarEvent event = TestObjectUtil.createCalendarEvent();
        CalendarEventDTO dto = calendarEventMapper.calendarEventToDTO(event);
        dto.setId(1L);
        CalendarEvent newEvent = calendarEventService.create(dto, user);
    }

    @Test
    public void eventIsUpdated() {
        User user = createUser();
        CalendarEvent event = TestObjectUtil.createCalendarEvent();
        Calendar calendar = calendarService.getCalendarForUser(user);
        event.setCalendar(calendar);
        event = calendarEventRepository.save(event);
        CalendarEventDTO updateDTO = calendarEventMapper.calendarEventToDTO(event);
        updateDTO.setTitle("Updated Title");
        CalendarEvent updated = calendarEventService.update(updateDTO);
        assertNotNull(updated);
        assertEquals(event.getId(), updated.getId());
        assertEquals(event.getCalendar().getId(), updated.getCalendar().getId());
        assertEquals(updateDTO.getTitle(), updated.getTitle());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUpdateIdThrowsException() {
        User user = createUser();
        CalendarEvent event = TestObjectUtil.createCalendarEvent();
        Calendar calendar = calendarService.getCalendarForUser(user);
        event.setCalendar(calendar);
        event = calendarEventRepository.save(event);
        CalendarEventDTO updateDTO = calendarEventMapper.calendarEventToDTO(event);
        updateDTO.setTitle("Updated Title");
        updateDTO.setId(updateDTO.getId() + 1);
        CalendarEvent updated = calendarEventService.update(updateDTO);
    }

    @Test
    public void returnEventsByUser() throws ParseException {
        User user = createUser(0);
        User antiUser = createUser(1);
        long seconds = 10000;
        List<CalendarEvent> userEvents = new ArrayList<>();
        List<CalendarEvent> allEvents = new ArrayList<>();
        for (int i = 0; i < USER_EVENT_COUNT; i++) {
            CalendarEvent event = createEvent(user, i + 1,
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC));
            userEvents.add(event);
            allEvents.add(event);

           event = createEvent(antiUser, i + 1,
               ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC));
            allEvents.add(event);
            seconds += seconds;
        }

        List<CalendarEvent> retrievedEvents = calendarEventService.findByUser(user);
        assertNotNull(retrievedEvents);
        assertEquals(USER_EVENT_COUNT, retrievedEvents.size());
        assertNotEquals(allEvents.size(), retrievedEvents.size());
        for (CalendarEvent event : retrievedEvents) {
            assertFalse(event.getTitle().contains("AntiEvent"));
        }
    }

    @Test
    public void returnsEventsByDay() {
        User user = createUser();
        // 3 events each for 2 days
        String dayOneString = "2015-01-01T10:15:30Z"; //Offset hours to test complete day coverage
        String dayTwoString = "2015-01-02T00:00:00Z";

        Pair<List<ZonedDateTime>, List<ZonedDateTime>> dates = getReferenceDates(dayOneString, dayTwoString, this::getTimesForDay);
        List<ZonedDateTime> timesForDayOne = dates.getLeft();
        List<ZonedDateTime> timesForDayTwo = dates.getRight();

        List<CalendarEvent> dayOneEvents = new ArrayList<>();
        List<CalendarEvent> dayTwoEvents = new ArrayList<>();
        for (int i = 0; i < timesForDayOne.size(); i++) {
            dayOneEvents.add(createEvent(user, i, timesForDayOne.get(i)));
            dayTwoEvents.add(createEvent(user, i * 10, timesForDayTwo.get(i)));
        }

        EventListDurationDTO dayOneDuration = new EventListDurationDTO(Duration.DAY, parseDate(dayOneString));
        List<CalendarEvent> dayOneRetrieval = calendarEventService.findByUserAndDuration(user, dayOneDuration);
        assertEquals(dayOneEvents.size(), dayOneRetrieval.size());
        for (CalendarEvent event : dayOneRetrieval) {
            assertTrue(containsById(dayOneEvents, event));
        }

        EventListDurationDTO dayTwoDuration = new EventListDurationDTO(Duration.DAY, parseDate(dayTwoString));
        List<CalendarEvent> dayTwoRetrieval = calendarEventService.findByUserAndDuration(user, dayTwoDuration);
        assertEquals(dayTwoEvents.size(), dayTwoRetrieval.size());
        for (CalendarEvent event : dayTwoRetrieval) {
            assertTrue(containsById(dayTwoEvents, event));
        }
    }

    @Test
    public void returnsEventsByMonth() {
        User user = createUser();
        // 3 events each for 2 days
        String monthOneString = "2015-01-11T10:15:30Z"; //Offset hours to test complete day coverage
        String monthTwoString = "2015-02-15T00:00:00Z";

        Pair<List<ZonedDateTime>, List<ZonedDateTime>> dates = getReferenceDates(monthOneString, monthTwoString, this::getTimesForMonth);
        List<ZonedDateTime> timesForMonthOne = dates.getLeft();
        List<ZonedDateTime> timesForMonthTwo = dates.getRight();

        List<CalendarEvent> monthOneEvents = new ArrayList<>();
        List<CalendarEvent> monthTwoEvents = new ArrayList<>();
        for (int i = 0; i < timesForMonthOne.size(); i++) {
            monthOneEvents.add(createEvent(user, i, timesForMonthOne.get(i)));
            monthTwoEvents.add(createEvent(user, i * 10, timesForMonthTwo.get(i)));
        }

        EventListDurationDTO monthOneDuration = new EventListDurationDTO(Duration.MONTH, parseDate(monthOneString));
        List<CalendarEvent> monthOneRetrieval = calendarEventService.findByUserAndDuration(user, monthOneDuration);
        assertEquals(monthOneEvents.size(), monthOneRetrieval.size());
        for (CalendarEvent event : monthOneRetrieval) {
            assertTrue(containsById(monthOneEvents, event));
        }

        EventListDurationDTO monthTwoDuration = new EventListDurationDTO(Duration.MONTH, parseDate(monthTwoString));
        List<CalendarEvent> monthTwoRetrieval = calendarEventService.findByUserAndDuration(user, monthTwoDuration);
        assertEquals(monthTwoEvents.size(), monthTwoRetrieval.size());
        for (CalendarEvent event : monthTwoRetrieval) {
            assertTrue(containsById(monthTwoEvents, event));
        }
    }

    @Test
    public void returnsEventsByWeek() {
        User user = createUser();
        // 3 events each for 2 weeks
        String weekOneString = "2015-01-01T10:15:30Z"; //Offset hours to test complete day coverage
        String weekTwoString = "2015-01-08T00:00:00Z";

        Pair<List<ZonedDateTime>, List<ZonedDateTime>> dates = getReferenceDates(weekOneString, weekTwoString, this::getTimesForWeek);
        List<ZonedDateTime> timesForWeekOne = dates.getLeft();
        List<ZonedDateTime> timesForWeekTwo = dates.getRight();

        List<CalendarEvent> weekOneEvents = new ArrayList<>();
        List<CalendarEvent> weekTwoEvents = new ArrayList<>();
        for (int i = 0; i < timesForWeekOne.size(); i++) {
            weekOneEvents.add(createEvent(user, i, timesForWeekOne.get(i)));
            weekTwoEvents.add(createEvent(user, i * 10, timesForWeekTwo.get(i)));
        }

        EventListDurationDTO weekOneDuration = new EventListDurationDTO(Duration.WEEK, parseDate(weekOneString));
        List<CalendarEvent> weekOneRetrieval = calendarEventService.findByUserAndDuration(user, weekOneDuration);
        assertEquals(weekOneEvents.size(), weekOneRetrieval.size());
        for (CalendarEvent event : weekOneRetrieval) {
            assertTrue(containsById(weekOneEvents, event));
        }

        EventListDurationDTO weekTwoDuration = new EventListDurationDTO(Duration.WEEK, parseDate(weekTwoString));
        List<CalendarEvent> weekTwoRetrieval = calendarEventService.findByUserAndDuration(user, weekTwoDuration);
        assertEquals(weekTwoEvents.size(), weekTwoRetrieval.size());
        for (CalendarEvent event : weekTwoRetrieval) {
            assertTrue(containsById(weekTwoEvents, event));
        }
    }

    protected Pair<List<ZonedDateTime>, List<ZonedDateTime>> getReferenceDates(String sourceDate, String controlDate,
                                                 Function<ZonedDateTime, List<ZonedDateTime>> timeFunction) {
        ZonedDateTime dateOne = parseDate(sourceDate);
        List<ZonedDateTime> sourceDates = timeFunction.apply(dateOne);

        ZonedDateTime dateTwo = parseDate(controlDate);
        List<ZonedDateTime> controlDates = timeFunction.apply(dateTwo);
        return Pair.of(sourceDates, controlDates);
    }

    private ZonedDateTime parseDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return ZonedDateTime.parse(date, formatter);
    }

    private boolean containsById(List<CalendarEvent> events, CalendarEvent event) {
        for (CalendarEvent listEvent : events) {
            if (listEvent.getId().equals(event.getId())) {
                return true;
            }
        }
        return false;
    }

    protected List<ZonedDateTime> getTimesForDay(ZonedDateTime sourceDay) {
        List<ZonedDateTime> times = getTimesForPeriod(sourceDay, ChronoUnit.HOURS);
        // sanity check
        for (ZonedDateTime time : times) {
            assertEquals(sourceDay.truncatedTo(ChronoUnit.DAYS), time.truncatedTo(ChronoUnit.DAYS));
        }
        return times;
    }

    protected List<ZonedDateTime> getTimesForMonth(ZonedDateTime sourceMonth) {
        return getTimesForPeriod(sourceMonth, ChronoUnit.DAYS);
    }

    protected List<ZonedDateTime> getTimesForWeek(ZonedDateTime sourceYear) {
        return getTimesForPeriod(sourceYear, ChronoUnit.DAYS);
    }

    private List<ZonedDateTime> getTimesForPeriod(ZonedDateTime sourceDate, ChronoUnit stepUnit) {
        ZonedDateTime truncatedDate = sourceDate.truncatedTo(ChronoUnit.DAYS);
        List<ZonedDateTime> times = new ArrayList<>();
        times.add(truncatedDate.plus(3, stepUnit));
        times.add(truncatedDate.plus(5, stepUnit));
        times.add(truncatedDate.plus(6, stepUnit));
        return times;
    }

    private CalendarEvent createEvent(User user, int index, ZonedDateTime time) {
        CalendarEvent event = TestObjectUtil.createCalendarEvent();
        event.setTitle(getEventTitle(user, index));
        event.setTime(time);
        CalendarEventDTO dto = calendarEventMapper.calendarEventToDTO(event);
        event = calendarEventService.create(dto, user);
        return event;
    }

    private String getEventTitle(User user, int index) {
        return user.getEmail() + "_" + index;
    }

    private User createUser() {
        return TestObjectUtil.createUser(userService);
    }

    private User createUser(int index) {
        return TestObjectUtil.createUser(userService, index);
    }
}
