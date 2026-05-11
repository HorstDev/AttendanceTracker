package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.DisciplineAhpExpertJudgment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisciplineAhpExpertJudgmentRepository extends JpaRepository<DisciplineAhpExpertJudgment, UUID> {

    Optional<DisciplineAhpExpertJudgment> findByDiscipline_IdAndTeacher_Id(UUID disciplineId, UUID teacherProfileId);

    @Query("SELECT j FROM DisciplineAhpExpertJudgment j JOIN FETCH j.teacher WHERE j.discipline.id = :disciplineId")
    List<DisciplineAhpExpertJudgment> findAllFetchedByDisciplineId(@Param("disciplineId") UUID disciplineId);

    void deleteByDiscipline_IdAndTeacher_Id(UUID disciplineId, UUID teacherProfileId);
}
