package org.astu.attendancetracker.core.application.common.dto.apitable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTableTimetable(List<ApiTableTimetableItem> timetable) {
}
