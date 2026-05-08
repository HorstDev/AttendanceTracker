package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Competency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetencyRepository extends JpaRepository<Competency, UUID> {
    Optional<Competency> findByAbbreviation(String abbreviation);

    Optional<Competency> findByAbbreviationAndGroup_Id(String abbreviation, UUID groupId);

    List<Competency> findByGroup_IdOrderByAbbreviationAsc(UUID groupId);
}
