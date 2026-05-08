package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.DisciplineCurriculumCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DisciplineCurriculumCompetencyRepository extends JpaRepository<DisciplineCurriculumCompetency, UUID> {

    @Query("select l from DisciplineCurriculumCompetency l "
            + "join fetch l.disciplineCurriculum dc "
            + "join fetch l.competency c "
            + "where dc.group.id = :groupId")
    List<DisciplineCurriculumCompetency> findAllFetchedByGroupId(@Param("groupId") UUID groupId);

    @Query("select l from DisciplineCurriculumCompetency l "
            + "join fetch l.competency "
            + "join fetch l.disciplineCurriculum dc "
            + "where dc.group.id = :groupId")
    List<DisciplineCurriculumCompetency> findByDisciplineCurriculum_Group_Id(@Param("groupId") UUID groupId);
}
