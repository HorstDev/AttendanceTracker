package org.astu.attendancetracker.core.application.common.dto.apitable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTableGroupSchedule(String name, List<ApiTableLesson> lessons) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiTableLesson(List<ApiTableEntry> entries, int dayId, int lessonOrderId) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ApiTableEntry(String teacher, String discipline, String type, String audience) {
        }
    }
}
