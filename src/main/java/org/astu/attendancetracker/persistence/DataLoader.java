package org.astu.attendancetracker.persistence;

import org.astu.attendancetracker.core.domain.Role;
import org.astu.attendancetracker.core.domain.User;
import org.astu.attendancetracker.persistence.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<User> usersToLoad = users();
        for (User user : usersToLoad) {
            if (userRepository.findByLogin(user.getLogin()).isEmpty()) {
                userRepository.save(user);
            };
        }
    }

    private List<User> users() {
        User admin = User
                .builder()
                .login("admin_astu")
                .password("$2a$10$unePMYhEhAvadfyPIy/H5uXExdCgAhj46j0Q2gij8WugLy37CrQni")
                .role(Role.ADMIN)
                .build();

        return List.of(admin);
    }
}
