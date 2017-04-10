package gvaughn.example.calendar.service;

import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.domain.User;
import gvaughn.example.calendar.repository.CalendarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class for managing calendar objects.
 *
 * Created by Graham Vaughn on 4/9/2017.
 */
@Service
@Transactional
public class CalendarService {

    private final Logger log = LoggerFactory.getLogger(CalendarService.class);

    private static final String CALENDAR_NAME_TEMPLATE = "Default Calendar for %s";

    @Autowired
    private CalendarRepository calendarRepository;

    /**
     * Returns the default Calendar for the given user. If no calendar exists for the user, one will be created.
     * @param user User for whom the default calendar will be returned.
     * @return The default calendar for the given user.
     */
    public Calendar getCalendarForUser(User user) {
        Optional<Calendar> calendar = calendarRepository.findByUser(user).stream().findFirst();
        return calendar.orElse(createUserCalendar(user));
    }

    protected Calendar createUserCalendar(User user) {
        Calendar calendar = new Calendar().name(String.format(CALENDAR_NAME_TEMPLATE, user.getEmail()));
        calendar.setUser(user);
        return calendarRepository.save(calendar);
    }
}
