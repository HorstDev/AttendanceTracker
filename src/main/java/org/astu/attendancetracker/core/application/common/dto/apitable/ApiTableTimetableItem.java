package org.astu.attendancetracker.core.application.common.dto.apitable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTableTimetableItem(String id, String name, String type) {
}
