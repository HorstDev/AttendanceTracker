package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

// Рейтинговый балл: несколько записей на пару (студент, дисциплина) допускаются
@Table(name = "rating_scores")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingScore {

    @Id
    @GeneratedValue
    private UUID id;

    private double score;
    private LocalDate deadlineDate;
    // null, пока балл не выставлен
    private LocalDate awardedDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "student_profile_id")
    private StudentProfile studentProfile;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "discipline_id")
    private Discipline discipline;
}
