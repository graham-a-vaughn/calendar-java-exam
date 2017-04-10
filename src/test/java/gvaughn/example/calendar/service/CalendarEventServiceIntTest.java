package gvaughn.example.calendar.service;

import gvaughn.example.calendar.CalendarApp;
import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.domain.User;
import gvaughn.example.calendar.repository.CalendarEventRepository;
import gvaughn.example.calendar.service.dto.CalendarEventDTO;
import gvaughn.example.calendar.service.mapper.CalendarEventMapper;
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
import java.util.ArrayList;
import java.util.List;

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
        String eventTitleTemplate = "Event #%d";
        String antiEventTitleTemplate = "AntiEvent #%d";
        long seconds = 10000;
        List<CalendarEvent> userEvents = new ArrayList<>();
        List<CalendarEvent> allEvents = new ArrayList<>();
        for (int i = 0; i < USER_EVENT_COUNT; i++) {
            CalendarEvent event = TestObjectUtil.createCalendarEvent();
            event.setTitle(String.format(eventTitleTemplate, (i + 1)));
            event.setTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC));
            CalendarEventDTO dto = calendarEventMapper.calendarEventToDTO(event);
            event = calendarEventService.create(dto, user);
            userEvents.add(event);
            allEvents.add(event);

            event = TestObjectUtil.createCalendarEvent();
            event.setTitle(String.format(eventTitleTemplate, (i + 1)));
            event.setTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC));
            dto = calendarEventMapper.calendarEventToDTO(event);
            calendarEventService.create(dto, antiUser);
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

    private User createUser() {
        return TestObjectUtil.createUser(userService);
    }

    private User createUser(int index) {
        return TestObjectUtil.createUser(userService, index);
    }
}
