package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.Profile;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @Query("SELECT t FROM TeacherProfile t")
    List<TeacherProfile> findAllTeacherProfiles();
}
