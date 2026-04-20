package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.LessonOutcome;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonOutcomeRepository extends JpaRepository<LessonOutcome, UUID> {

    @Query(value = "SELECT lo.* FROM lesson_outcomes lo " +
            "JOIN lessons l ON l.id = lo.lesson_id " +
            "JOIN disciplines d ON d.id = l.discipline_id " +
            "JOIN teacher_discipline td ON d.id = td.discipline_id " +
            "WHERE l.is_started = true " +
            "AND clock_timestamp() BETWEEN l.real_start_dt AND l.real_end_dt " +
            "AND td.teacher_id = :teacherId", nativeQuery = true)
    List<LessonOutcome> findOutcomesForInProgressLessonsByTeacher(@Param("teacherId") UUID teacherId);

    @Query(value = "SELECT lo.* FROM lesson_outcomes lo " +
            "JOIN lessons l ON l.id = lo.lesson_id " +
            "WHERE lo.student_id = :studentId " +
            "AND l.is_started = true " +
            "AND clock_timestamp() BETWEEN l.real_start_dt AND l.real_end_dt " +
            "LIMIT 1", nativeQuery = true)
    Optional<LessonOutcome> findActiveOutcomeByStudentId(@Param("studentId") UUID studentId);
}
