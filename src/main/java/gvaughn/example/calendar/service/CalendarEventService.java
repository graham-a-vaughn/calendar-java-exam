package gvaughn.example.calendar.service;

import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.domain.User;
import gvaughn.example.calendar.repository.CalendarEventRepository;
import gvaughn.example.calendar.service.dto.CalendarEventDTO;
import gvaughn.example.calendar.service.dto.EventListDurationDTO;
import gvaughn.example.calendar.service.mapper.CalendarEventMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service Implementation for managing CalendarEvent.
 */
@Service
@Transactional
public class CalendarEventService {

    private final Logger log = LoggerFactory.getLogger(CalendarEventService.class);

    private final CalendarEventRepository calendarEventRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarEventMapper calendarEventMapper;

    @Autowired
    private CalendarEventReminderService calendarEventReminderService;

    public CalendarEventService(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    /**
     * Create a new calendar event for the given user.
     * @param dto Event DTO.
     * @param user Event owner.
     * @return The newly created calendar event.
     */
    public CalendarEvent create(CalendarEventDTO dto, User user) {
        Objects.requireNonNull(dto);
        Objects.requireNonNull(user);
        if (dto.getId() != null) {
            throw new IllegalArgumentException("Create requires a null id.");
        }
        CalendarEvent event = calendarEventMapper.dtoToCalendarEvent(dto);
        Calendar calendar = calendarService.getCalendarForUser(user);
        event.setCalendar(calendar);
        event = calendarEventRepository.save(event);
        calendarEventReminderService.scheduleReminder(event);
        return event;
    }

    /**
     * Update a calendar event.
     * @param dto DTO with updated event data.
     * @return Updated calendar event.
     */
    public CalendarEvent update(CalendarEventDTO dto) {
        Objects.requireNonNull(dto);
        Objects.requireNonNull(dto.getId());
        CalendarEvent updated = calendarEventMapper.dtoToCalendarEvent(dto);
        CalendarEvent event = Optional.ofNullable(calendarEventRepository.findOne(dto.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Invalid calendar event id: " + dto.getId()));

        updated.setCalendar(event.getCalendar());
        updated.setReminderSent(event.isReminderSent());
        if (!reminderTimesEqual(event, updated)) {
            calendarEventReminderService.updateReminder(updated);
        }
        return calendarEventRepository.save(updated);
    }

    private boolean reminderTimesEqual(CalendarEvent event, CalendarEvent updated) {
        if (event.getReminderTime() == null ) {
            return updated.getReminderTime() == null;
        }
        if (updated.getReminderTime() != null) {
            return updated.getReminderTime().truncatedTo(ChronoUnit.MINUTES)
                .equals(event.getReminderTime().truncatedTo(ChronoUnit.MINUTES));
        }
        return false;
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
     * Returns all calendar events for the given user.
     * @param user User whose events will be returned.
     * @return All calendar events for the given user.
     */
    @Transactional(readOnly = true)
    public List<CalendarEvent> findByUser(User user) {
        Objects.requireNonNull(user);
        Calendar calendar = calendarService.getCalendarForUser(user);
        return calendarEventRepository.findByCalendarOrderByTime(calendar);
    }

    @Transactional(readOnly = true)
    public List<CalendarEvent> findByUserAndDuration(User user, EventListDurationDTO durationDTO) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(durationDTO);
        Calendar calendar = calendarService.getCalendarForUser(user);
        Pair<ZonedDateTime, ZonedDateTime> dates = getDurationDates(durationDTO);
        return calendarEventRepository.findByCalendarAndTimeBetweenOrderByTime(calendar, dates.getLeft(), dates.getRight());
    }

    protected Pair<ZonedDateTime, ZonedDateTime> getDurationDates(EventListDurationDTO durationDTO) {
        ZonedDateTime submittedStartDate = durationDTO.getStartDate();
        ZonedDateTime startDate = submittedStartDate.truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime endDate = startDate.plusDays(durationDTO.getDuration().getDays());
        return Pair.of(startDate, endDate);
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
        calendarEventReminderService.cancelReminder(id);
        calendarEventRepository.delete(id);
    }
}
