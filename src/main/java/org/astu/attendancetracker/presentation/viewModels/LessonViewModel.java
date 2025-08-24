package org.astu.attendancetracker.presentation.viewModels;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LessonViewModel {
    private UUID id;
    private String subjectName;
    private String groupName;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime realStart;
    private LocalDateTime realEnd;
    private String type;
    private boolean isStarted;
}
