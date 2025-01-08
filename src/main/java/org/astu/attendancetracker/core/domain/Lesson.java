package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.astu.attendancetracker.core.application.common.enums.LessonType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(name = "lessons")
@Entity
@Getter
@Setter
public class Lesson {
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDateTime startDt;
    private LocalDateTime endDt;
    private LocalDateTime realStartDt;
    private LocalDateTime realEndDt;
    private LessonType lessonType;
    private String audience;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "discipline_id")
    private Discipline discipline;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LessonOutcome> lessonOutcomes;
}
