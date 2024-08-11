package org.astu.attendancetracker.core.application.common.dto.apitable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTableGlobalData(ApiTableConfig config) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiTableConfig(int weekOverride) {
    }
}
