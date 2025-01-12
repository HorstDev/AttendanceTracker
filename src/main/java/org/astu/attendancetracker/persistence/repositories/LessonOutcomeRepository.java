package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.LessonOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LessonOutcomeRepository extends JpaRepository<LessonOutcome, UUID> {
}
