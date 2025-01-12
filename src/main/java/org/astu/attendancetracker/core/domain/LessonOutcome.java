package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name = "lesson_outcomes")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonOutcome {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "student_id")
    private StudentProfile studentProfile;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    private boolean isVisited;
}
