package gvaughn.example.calendar.repository;

import gvaughn.example.calendar.domain.Calendar;

import gvaughn.example.calendar.domain.User;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Calendar entity.
 */
@SuppressWarnings("unused")
public interface CalendarRepository extends JpaRepository<Calendar,Long> {

    List<Calendar> findByUser(User user);
}
