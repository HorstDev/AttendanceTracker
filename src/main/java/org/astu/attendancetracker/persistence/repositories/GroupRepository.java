package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
}
