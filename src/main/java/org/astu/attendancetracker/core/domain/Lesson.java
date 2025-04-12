package org.astu.attendancetracker.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.astu.attendancetracker.core.application.common.enums.LessonType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(name = "lessons")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDateTime startDt;
    private LocalDateTime endDt;
    private boolean isStarted;
    private LocalDateTime realStartDt;
    private LocalDateTime realEndDt;
    private LessonType lessonType;
    private String audience;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "discipline_id")
    private Discipline discipline;

    @JsonIgnore
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LessonOutcome> lessonOutcomes;

    // Старт занятия
    public void start() {
        isStarted = true;

        var now = LocalDateTime.now();
        // Если занятие было начато по расписанию, то выставляем реальное время начала и конца такое же, как в расписании
        if (now.isAfter(startDt) && now.isBefore(endDt)) {
            realStartDt = startDt;
            realEndDt = endDt;
        } else {
            realStartDt = LocalDateTime.now();
            realEndDt = LocalDateTime.now().plusMinutes(90);
        }
    }
}
