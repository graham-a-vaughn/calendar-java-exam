package gvaughn.example.calendar.service.mapper;

import gvaughn.example.calendar.domain.CalendarEvent;
import gvaughn.example.calendar.service.dto.CalendarEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps CalendarEvent entities to CalendarEventDTOs.
 *
 * Created by Graham Vaughn on 4/9/2017.
 */
@Mapper(componentModel = "spring", uses = {})
@Component
public interface CalendarEventMapper {

    CalendarEventDTO calendarEventToDTO(CalendarEvent calendarEvent);

    List<CalendarEventDTO> calendarEventsToDTOs(List<CalendarEvent> calendarEvents);

    @Mapping(target = "calendar", ignore = true)
    @Mapping(target = "reminderSent", ignore = true)
    CalendarEvent dtoToCalendarEvent(CalendarEventDTO dto);

    List<CalendarEvent> dtosToCalendarEvents(List<CalendarEventDTO> dtos);
}
