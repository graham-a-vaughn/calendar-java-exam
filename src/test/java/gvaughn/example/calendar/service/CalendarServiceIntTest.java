package gvaughn.example.calendar.service;

import gvaughn.example.calendar.CalendarApp;
import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.domain.User;
import gvaughn.example.calendar.repository.CalendarRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Graham Vaughn on 4/9/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CalendarApp.class)
@Transactional
public class CalendarServiceIntTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarRepository calendarRepository;

    @Test
    public void calendarIsCreatedIfNonExistent() {
        User user = createUser();
        Calendar calendar = calendarService.getCalendarForUser(user);
        assertNotNull(calendar);
        assertNotNull(calendar.getName());
        assertNotNull(calendar.getUser());
        assertEquals(user.getId(), calendar.getUser().getId());
    }

    @Test
    public void existingCalendarIsReturned() {
        User user = createUser();
        Calendar calendar = createCalendar();
        calendar.setUser(user);
        Calendar saved = calendarRepository.save(calendar);
        Calendar retrieved = calendarService.getCalendarForUser(user);
        assertNotNull(retrieved);
        assertEquals(saved.getId(), retrieved.getId());
    }

    public static Calendar createCalendar() {
        return new Calendar().name("Default test calendar");
    }
    private User createUser() {
        return TestObjectUtil.createUser(userService);
    }
}
