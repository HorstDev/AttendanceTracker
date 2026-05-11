package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.RatingScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RatingScoreRepository extends JpaRepository<RatingScore, UUID> {

    @Query("SELECT COALESCE(SUM(r.score), 0) FROM RatingScore r WHERE r.studentProfile.id = :studentId AND r.discipline.id = :disciplineId")
    double sumScoreForStudentAndDiscipline(@Param("studentId") UUID studentId, @Param("disciplineId") UUID disciplineId);
}
