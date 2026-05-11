package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.DisciplineImportanceWeights;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DisciplineImportanceWeightsRepository extends JpaRepository<DisciplineImportanceWeights, UUID> {

    Optional<DisciplineImportanceWeights> findByDiscipline_Id(UUID disciplineId);
}
