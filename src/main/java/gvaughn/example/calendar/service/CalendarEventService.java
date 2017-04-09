package gvaughn.example.calendar.service;

import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.repository.CalendarEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing CalendarEvent.
 */
@Service
@Transactional
public class CalendarEventService {

    private final Logger log = LoggerFactory.getLogger(CalendarEventService.class);
    
    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    /**
     * Save a calendarEvent.
     *
     * @param calendarEvent the entity to save
     * @return the persisted entity
     */
    public CalendarEvent save(CalendarEvent calendarEvent) {
        log.debug("Request to save CalendarEvent : {}", calendarEvent);
        CalendarEvent result = calendarEventRepository.save(calendarEvent);
        return result;
    }

    /**
     *  Get all the calendarEvents.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<CalendarEvent> findAll() {
        log.debug("Request to get all CalendarEvents");
        List<CalendarEvent> result = calendarEventRepository.findAll();

        return result;
    }

    /**
     *  Get one calendarEvent by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public CalendarEvent findOne(Long id) {
        log.debug("Request to get CalendarEvent : {}", id);
        CalendarEvent calendarEvent = calendarEventRepository.findOne(id);
        return calendarEvent;
    }

    /**
     *  Delete the  calendarEvent by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete CalendarEvent : {}", id);
        calendarEventRepository.delete(id);
    }
}
