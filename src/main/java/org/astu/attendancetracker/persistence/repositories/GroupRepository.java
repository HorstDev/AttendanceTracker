package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    @Query("SELECT g FROM Group g WHERE g.name LIKE concat('%', :partOfName, '%')")
    List<Group> findAllGroupsByPartOfName(@Param("partOfName") String partOfName);
}
