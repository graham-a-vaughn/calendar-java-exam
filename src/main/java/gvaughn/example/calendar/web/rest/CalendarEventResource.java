package gvaughn.example.calendar.web.rest;

import com.codahale.metrics.annotation.Timed;
import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.domain.User;
import gvaughn.example.calendar.service.CalendarEventService;
import gvaughn.example.calendar.service.UserService;
import gvaughn.example.calendar.service.dto.CalendarEventDTO;
import gvaughn.example.calendar.service.dto.Duration;
import gvaughn.example.calendar.service.dto.EventListDurationDTO;
import gvaughn.example.calendar.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing CalendarEvent.
 */
@RestController
@RequestMapping("/api")
public class CalendarEventResource {

    private final Logger log = LoggerFactory.getLogger(CalendarEventResource.class);

    private static final String ENTITY_NAME = "calendarEvent";

    private final CalendarEventService calendarEventService;

    @Autowired
    private UserService userService;

    public CalendarEventResource(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    /**
     * POST  /calendar-events : Create a new calendarEvent.
     *
     * @param calendarEvent the calendarEvent to create
     * @return the ResponseEntity with status 201 (Created) and with body the new calendarEvent, or with status 400 (Bad Request) if the calendarEvent has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/calendar-events")
    @Timed
    public ResponseEntity<CalendarEvent> createCalendarEvent(@Valid @RequestBody CalendarEventDTO calendarEvent) throws URISyntaxException {
        log.debug("REST request to create CalendarEvent : {}", calendarEvent);
        if (calendarEvent.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new calendarEvent cannot already have an ID")).body(null);
        }
        User currentUser = userService.getUserWithAuthorities();
        CalendarEvent result = calendarEventService.create(calendarEvent, currentUser);
        return ResponseEntity.created(new URI("/api/calendar-events/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /calendar-events : Updates an existing calendarEvent.
     *
     * @param calendarEvent the calendarEvent to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated calendarEvent,
     * or with status 400 (Bad Request) if the calendarEvent is not valid,
     * or with status 500 (Internal Server Error) if the calendarEvent couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/calendar-events")
    @PreAuthorize("@calendarEventPermissions.isOwner(calendarEvent)")
    @Timed
    public ResponseEntity<CalendarEvent> updateCalendarEvent(@Valid @RequestBody CalendarEventDTO calendarEvent) throws URISyntaxException {
        log.debug("REST request to update CalendarEvent : {}", calendarEvent);
        if (calendarEvent.getId() == null) {
            return createCalendarEvent(calendarEvent);
        }
        CalendarEvent result = calendarEventService.update(calendarEvent);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, calendarEvent.getId().toString()))
            .body(result);
    }

    /**
     * GET  /calendar-events : get all the calendarEvents for the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of calendarEvents in body
     */
    @GetMapping("/calendar-events")
    @Timed
    public List<CalendarEvent> getAllCalendarEvents(@RequestParam(required = false) EventListDurationDTO durationDTO) {
        log.debug("REST request to get all CalendarEvents");
        User currentUser = userService.getUserWithAuthorities();
        List<CalendarEvent> results = new ArrayList<>();
        if (durationDTO != null) {
            results.addAll(calendarEventService.findByUserAndDuration(currentUser, durationDTO));
        } else {
            results.addAll(calendarEventService.findByUser(currentUser));
        }
        return results;
    }

    /**
     * GET  /calendar-events/:id : get the "id" calendarEvent.
     *
     * @param id the id of the calendarEvent to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the calendarEvent, or with status 404 (Not Found)
     */
    @GetMapping("/calendar-events/{id}")
    @Timed
    public ResponseEntity<CalendarEvent> getCalendarEvent(@PathVariable Long id) {
        log.debug("REST request to get CalendarEvent : {}", id);
        CalendarEvent calendarEvent = calendarEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(calendarEvent));
    }

    /**
     * DELETE  /calendar-events/:id : delete the "id" calendarEvent.
     *
     * @param id the id of the calendarEvent to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/calendar-events/{id}")
    @PreAuthorize("@calendarEventPermissions.isOwner(calendarEvent)")
    @Timed
    public ResponseEntity<Void> deleteCalendarEvent(@PathVariable Long id) {
        log.debug("REST request to delete CalendarEvent : {}", id);
        calendarEventService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
