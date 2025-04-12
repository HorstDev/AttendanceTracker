package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Lesson;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    // Извлекает профили студентов, участвующих в занятии
    @Query("SELECT sp FROM StudentProfile sp WHERE sp.group.id IN " +
            "   (SELECT d.group.id FROM Discipline d WHERE d.id IN " +
            "       (SELECT l.discipline.id FROM Lesson l WHERE l.id = :lessonId))")
    List<StudentProfile> findStudentsParticipatingInLesson(UUID lessonId);

    @Query("SELECT l FROM Lesson l WHERE l.discipline.group.id = :groupId AND l.isStarted")
    List<Lesson> findStartedLessonsInGroup(UUID groupId);

    @Query(value = "SELECT l.* FROM lessons l " +
            "JOIN disciplines d ON d.id = l.discipline_id " +
            "JOIN teacher_discipline td ON d.id = td.discipline_id " +
            "WHERE CAST(l.start_dt AS DATE) = :date " +
            "AND d.semester = (SELECT MAX(d1.semester) FROM disciplines d1 WHERE d1.group_id = d.group_id) " +
            "AND td.teacher_id = :teacherId", nativeQuery = true)
    List<Lesson> findLessonsByDateAndTeacher(@Param("teacherId") UUID teacherId, @Param("date") LocalDate date);
}
