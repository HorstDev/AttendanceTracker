package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Lesson;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    // Извлекает профили студентов, участвующих в занятии
    @Query("SELECT sp FROM StudentProfile sp WHERE sp.group.id IN " +
            "   (SELECT d.group.id FROM Discipline d WHERE d.id IN " +
            "       (SELECT l.discipline.id FROM Lesson l WHERE l.id = :lessonId))")
    List<StudentProfile> findStudentsParticipatingInLesson(UUID lessonId);
}
