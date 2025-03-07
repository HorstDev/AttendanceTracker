package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Profile;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @Query("SELECT t FROM TeacherProfile t")
    List<TeacherProfile> findAllTeacherProfiles();

    @Query("SELECT t FROM TeacherProfile t WHERE t.name LIKE concat('%', :partOfName, '%')")
    Optional<List<TeacherProfile>> findAllTeacherProfilesByPartOfName(@Param("partOfName") String partOfName);

    @Query("SELECT t FROM TeacherProfile t WHERE t.name IN :names")
    Set<TeacherProfile> findAllTeacherProfilesByNameIn(@Param("names") Set<String> names);
}
