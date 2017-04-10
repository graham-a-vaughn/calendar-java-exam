package gvaughn.example.calendar.repository;

import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.domain.CalendarEvent;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the CalendarEvent entity.
 */
@SuppressWarnings("unused")
public interface CalendarEventRepository extends JpaRepository<CalendarEvent,Long> {

    List<CalendarEvent> findByCalendarOrderByTime(Calendar calendar);
}
