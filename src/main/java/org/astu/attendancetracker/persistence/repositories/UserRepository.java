package org.astu.attendancetracker.persistence.repositories;

import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.core.domain.User;
import org.astu.attendancetracker.presentation.viewModels.AuthorizationDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByLogin(String login);

    @Modifying
    @Query("UPDATE User u SET u.login = :login, u.password = :password WHERE u.profile.id = :profileId")
    int updateAuthenticationData(@Param("profileId") UUID profileId, @Param("login") String login, @Param("password") String password);
}
