package gvaughn.example.calendar.security;

import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.domain.User;
import gvaughn.example.calendar.service.CalendarEventService;
import gvaughn.example.calendar.service.UserService;
import gvaughn.example.calendar.service.dto.CalendarEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean for determining if the current user has permission to access
 * a calendar event.
 *
 * Created by Graham Vaughn on 4/11/2017.
 */
@Component("calendarEventPermissions")
public class CalendarEventPermissions {

    @Autowired
    private UserService userService;

    @Autowired
    private CalendarEventService calendarEventService;

    public boolean isOwner(CalendarEventDTO calendarEvent) {
        boolean owner = false;
        if (calendarEvent != null) {
            CalendarEvent event = calendarEventService.findOne(calendarEvent.getId());
            User user = userService.getUserWithAuthorities();
            if(event != null && user != null) {
                owner = (event.getCalendar().getUser().getId().equals(user.getId()));
            }
        }
        return owner;
    }
}
