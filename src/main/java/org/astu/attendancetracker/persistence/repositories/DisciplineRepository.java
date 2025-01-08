package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisciplineRepository extends JpaRepository<Discipline, UUID> {

    List<Discipline> findByGroupAndSemester(Group group, int semester);
}
