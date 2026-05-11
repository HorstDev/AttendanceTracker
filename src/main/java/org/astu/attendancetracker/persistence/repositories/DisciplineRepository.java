package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisciplineRepository extends JpaRepository<Discipline, UUID> {

    @Query("SELECT DISTINCT d FROM Discipline d JOIN FETCH d.group JOIN FETCH d.teacherProfiles WHERE d.id = :id")
    Optional<Discipline> findByIdFetchedTeachers(@Param("id") UUID id);

    @Query("SELECT DISTINCT d FROM Discipline d JOIN FETCH d.group JOIN d.teacherProfiles t WHERE t.id = :teacherProfileId")
    List<Discipline> findDistinctByTeacherProfileId(@Param("teacherProfileId") UUID teacherProfileId);

    List<Discipline> findByGroupAndSemester(Group group, int semester);

    @Query("SELECT d FROM Discipline d WHERE d.group.id = :groupId AND d.semester = " +
            "(SELECT MAX(d2.semester) FROM Discipline d2 WHERE d2.group.id = :groupId)")
    List<Discipline> findByGroupInCurrentSemester(UUID groupId);

    List<Discipline> findByGroup(Group group);
}
