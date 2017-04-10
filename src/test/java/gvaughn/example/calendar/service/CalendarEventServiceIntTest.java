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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Graham Vaughn on 4/10/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CalendarApp.class)
@Transactional
public class CalendarEventServiceIntTest {

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

    private User createUser() {
        return TestObjectUtil.createUser(userService);
    }
}
