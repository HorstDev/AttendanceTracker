package org.astu.attendancetracker.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Матрица парных сравнений Саати (3×3 для A, G, L), введённая одним преподавателем по дисциплине.
 */
@Entity
@Table(
        name = "discipline_ahp_expert_judgments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"discipline_id", "teacher_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class DisciplineAhpExpertJudgment {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discipline_id", nullable = false)
    private Discipline discipline;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherProfile teacher;

    @Column(nullable = false)
    private double ratioAttendanceVsRating;

    @Column(nullable = false)
    private double ratioAttendanceVsLate;

    @Column(nullable = false)
    private double ratioRatingVsLate;

    @Column(nullable = false)
    private double weightAttendance;

    @Column(nullable = false)
    private double weightRating;

    @Column(nullable = false)
    private double weightLateSubmission;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public void applyComputed(
            double ratioAttendanceVsRating,
            double ratioAttendanceVsLate,
            double ratioRatingVsLate,
            double weightAttendance,
            double weightRating,
            double weightLateSubmission
    ) {
        this.ratioAttendanceVsRating = ratioAttendanceVsRating;
        this.ratioAttendanceVsLate = ratioAttendanceVsLate;
        this.ratioRatingVsLate = ratioRatingVsLate;
        this.weightAttendance = weightAttendance;
        this.weightRating = weightRating;
        this.weightLateSubmission = weightLateSubmission;
        this.updatedAt = Instant.now();
    }
}
