package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.DisciplineCurriculum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisciplineCurriculumRepository extends JpaRepository<DisciplineCurriculum, UUID> {

    void deleteByGroup_Id(UUID groupId);

    List<DisciplineCurriculum> findByGroup_Id(UUID groupId);
}
