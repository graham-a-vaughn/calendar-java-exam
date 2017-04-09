package gvaughn.example.calendar.web.rest;

import gvaughn.example.calendar.CalendarApp;

import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.domain.Calendar;
import gvaughn.example.calendar.repository.CalendarEventRepository;
import gvaughn.example.calendar.service.CalendarEventService;
import gvaughn.example.calendar.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static gvaughn.example.calendar.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CalendarEventResource REST controller.
 *
 * @see CalendarEventResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CalendarApp.class)
public class CalendarEventResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_REMINDER_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_REMINDER_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Boolean DEFAULT_REMINDER_SENT = false;
    private static final Boolean UPDATED_REMINDER_SENT = true;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private CalendarEventService calendarEventService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restCalendarEventMockMvc;

    private CalendarEvent calendarEvent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CalendarEventResource calendarEventResource = new CalendarEventResource(calendarEventService);
        this.restCalendarEventMockMvc = MockMvcBuilders.standaloneSetup(calendarEventResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CalendarEvent createEntity(EntityManager em) {
        CalendarEvent calendarEvent = new CalendarEvent()
            .title(DEFAULT_TITLE)
            .time(DEFAULT_TIME)
            .location(DEFAULT_LOCATION)
            .reminderTime(DEFAULT_REMINDER_TIME)
            .reminderSent(DEFAULT_REMINDER_SENT);
        // Add required entity
        Calendar calendar = CalendarResourceIntTest.createEntity(em);
        em.persist(calendar);
        em.flush();
        calendarEvent.setCalendar(calendar);
        return calendarEvent;
    }

    @Before
    public void initTest() {
        calendarEvent = createEntity(em);
    }

    @Test
    @Transactional
    public void createCalendarEvent() throws Exception {
        int databaseSizeBeforeCreate = calendarEventRepository.findAll().size();

        // Create the CalendarEvent
        restCalendarEventMockMvc.perform(post("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(calendarEvent)))
            .andExpect(status().isCreated());

        // Validate the CalendarEvent in the database
        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeCreate + 1);
        CalendarEvent testCalendarEvent = calendarEventList.get(calendarEventList.size() - 1);
        assertThat(testCalendarEvent.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testCalendarEvent.getTime()).isEqualTo(DEFAULT_TIME);
        assertThat(testCalendarEvent.getLocation()).isEqualTo(DEFAULT_LOCATION);
        assertThat(testCalendarEvent.getReminderTime()).isEqualTo(DEFAULT_REMINDER_TIME);
        assertThat(testCalendarEvent.isReminderSent()).isEqualTo(DEFAULT_REMINDER_SENT);
    }

    @Test
    @Transactional
    public void createCalendarEventWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = calendarEventRepository.findAll().size();

        // Create the CalendarEvent with an existing ID
        calendarEvent.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCalendarEventMockMvc.perform(post("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(calendarEvent)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = calendarEventRepository.findAll().size();
        // set the field null
        calendarEvent.setTitle(null);

        // Create the CalendarEvent, which fails.

        restCalendarEventMockMvc.perform(post("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(calendarEvent)))
            .andExpect(status().isBadRequest());

        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = calendarEventRepository.findAll().size();
        // set the field null
        calendarEvent.setTime(null);

        // Create the CalendarEvent, which fails.

        restCalendarEventMockMvc.perform(post("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(calendarEvent)))
            .andExpect(status().isBadRequest());

        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkReminderTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = calendarEventRepository.findAll().size();
        // set the field null
        calendarEvent.setReminderTime(null);

        // Create the CalendarEvent, which fails.

        restCalendarEventMockMvc.perform(post("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(calendarEvent)))
            .andExpect(status().isBadRequest());

        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkReminderSentIsRequired() throws Exception {
        int databaseSizeBeforeTest = calendarEventRepository.findAll().size();
        // set the field null
        calendarEvent.setReminderSent(null);

        // Create the CalendarEvent, which fails.

        restCalendarEventMockMvc.perform(post("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(calendarEvent)))
            .andExpect(status().isBadRequest());

        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCalendarEvents() throws Exception {
        // Initialize the database
        calendarEventRepository.saveAndFlush(calendarEvent);

        // Get all the calendarEventList
        restCalendarEventMockMvc.perform(get("/api/calendar-events?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(calendarEvent.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].time").value(hasItem(sameInstant(DEFAULT_TIME))))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION.toString())))
            .andExpect(jsonPath("$.[*].reminderTime").value(hasItem(sameInstant(DEFAULT_REMINDER_TIME))))
            .andExpect(jsonPath("$.[*].reminderSent").value(hasItem(DEFAULT_REMINDER_SENT.booleanValue())));
    }

    @Test
    @Transactional
    public void getCalendarEvent() throws Exception {
        // Initialize the database
        calendarEventRepository.saveAndFlush(calendarEvent);

        // Get the calendarEvent
        restCalendarEventMockMvc.perform(get("/api/calendar-events/{id}", calendarEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(calendarEvent.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.time").value(sameInstant(DEFAULT_TIME)))
            .andExpect(jsonPath("$.location").value(DEFAULT_LOCATION.toString()))
            .andExpect(jsonPath("$.reminderTime").value(sameInstant(DEFAULT_REMINDER_TIME)))
            .andExpect(jsonPath("$.reminderSent").value(DEFAULT_REMINDER_SENT.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingCalendarEvent() throws Exception {
        // Get the calendarEvent
        restCalendarEventMockMvc.perform(get("/api/calendar-events/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCalendarEvent() throws Exception {
        // Initialize the database
        calendarEventService.save(calendarEvent);

        int databaseSizeBeforeUpdate = calendarEventRepository.findAll().size();

        // Update the calendarEvent
        CalendarEvent updatedCalendarEvent = calendarEventRepository.findOne(calendarEvent.getId());
        updatedCalendarEvent
            .title(UPDATED_TITLE)
            .time(UPDATED_TIME)
            .location(UPDATED_LOCATION)
            .reminderTime(UPDATED_REMINDER_TIME)
            .reminderSent(UPDATED_REMINDER_SENT);

        restCalendarEventMockMvc.perform(put("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCalendarEvent)))
            .andExpect(status().isOk());

        // Validate the CalendarEvent in the database
        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeUpdate);
        CalendarEvent testCalendarEvent = calendarEventList.get(calendarEventList.size() - 1);
        assertThat(testCalendarEvent.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testCalendarEvent.getTime()).isEqualTo(UPDATED_TIME);
        assertThat(testCalendarEvent.getLocation()).isEqualTo(UPDATED_LOCATION);
        assertThat(testCalendarEvent.getReminderTime()).isEqualTo(UPDATED_REMINDER_TIME);
        assertThat(testCalendarEvent.isReminderSent()).isEqualTo(UPDATED_REMINDER_SENT);
    }

    @Test
    @Transactional
    public void updateNonExistingCalendarEvent() throws Exception {
        int databaseSizeBeforeUpdate = calendarEventRepository.findAll().size();

        // Create the CalendarEvent

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restCalendarEventMockMvc.perform(put("/api/calendar-events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(calendarEvent)))
            .andExpect(status().isCreated());

        // Validate the CalendarEvent in the database
        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteCalendarEvent() throws Exception {
        // Initialize the database
        calendarEventService.save(calendarEvent);

        int databaseSizeBeforeDelete = calendarEventRepository.findAll().size();

        // Get the calendarEvent
        restCalendarEventMockMvc.perform(delete("/api/calendar-events/{id}", calendarEvent.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<CalendarEvent> calendarEventList = calendarEventRepository.findAll();
        assertThat(calendarEventList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CalendarEvent.class);
    }
}
