package org.astu.attendancetracker.presentation.viewModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonUserStatusViewModel {
    private UUID id;
    @JsonProperty("isVisited")
    private boolean isVisited;
    private Double score;
}
