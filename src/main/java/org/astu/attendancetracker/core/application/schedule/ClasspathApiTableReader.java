package org.astu.attendancetracker.core.application.schedule;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetable;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class ClasspathApiTableReader {

    public static final String TEACHERS_RESOURCE = "apitable.json";
    public static final String GROUP_SCHEDULE_RESOURCE = "ДИПРБ-41.json";

    private final ObjectMapper objectMapper;

    public ClasspathApiTableReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ApiTableTimetableItem> readTeachers() throws IOException {
        try (InputStream inputStream = requireResource(TEACHERS_RESOURCE)) {
            ApiTableTimetable timetable = objectMapper.readValue(inputStream, ApiTableTimetable.class);
            return timetable.timetable().stream()
                    .filter(item -> "teacher".equals(item.type()))
                    .toList();
        }
    }

    public ApiTableGroupSchedule readGroupSchedule() throws IOException {
        try (InputStream inputStream = requireResource(GROUP_SCHEDULE_RESOURCE)) {
            return objectMapper.readValue(inputStream, ApiTableGroupSchedule.class);
        }
    }

    /** Читает {@code config.weekOverride} из apitable.json без полной десериализации расписания. */
    public int readCurrentWeekNumber() throws IOException {
        try (InputStream inputStream = requireResource(TEACHERS_RESOURCE);
             JsonParser parser = objectMapper.getFactory().createParser(inputStream)) {
            while (parser.nextToken() != null) {
                if (parser.currentToken() == JsonToken.FIELD_NAME && "weekOverride".equals(parser.getCurrentName())) {
                    parser.nextToken();
                    return parser.getIntValue();
                }
            }
        }
        throw new IOException("weekOverride не найден в " + TEACHERS_RESOURCE);
    }

    private InputStream requireResource(String resourceName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new IOException("Файл не найден в resources: " + resourceName);
        }
        return inputStream;
    }
}
