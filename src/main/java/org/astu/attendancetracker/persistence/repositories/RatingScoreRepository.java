package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.RatingScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RatingScoreRepository extends JpaRepository<RatingScore, UUID> {

    @Query("SELECT COALESCE(SUM(r.score), 0) FROM RatingScore r WHERE r.studentProfile.id = :studentId AND r.discipline.id = :disciplineId")
    double sumScoreForStudentAndDiscipline(@Param("studentId") UUID studentId, @Param("disciplineId") UUID disciplineId);

    @Modifying
    @Query("DELETE FROM RatingScore r WHERE r.studentProfile.id = :studentId AND r.discipline.id = :disciplineId")
    void deleteByStudentProfile_IdAndDiscipline_Id(@Param("studentId") UUID studentId, @Param("disciplineId") UUID disciplineId);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RatingScore r
            WHERE r.studentProfile.id = :studentId AND r.discipline.id = :disciplineId
            AND r.awardedDate IS NOT NULL AND r.deadlineDate IS NOT NULL AND r.awardedDate > r.deadlineDate
            """)
    boolean hasLateSubmissionForStudentAndDiscipline(
            @Param("studentId") UUID studentId,
            @Param("disciplineId") UUID disciplineId
    );
}
