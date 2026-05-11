package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Пути с заглавной G — как ожидает Angular-клиент ({@code /api/v1/Group/...}).
 */
@RestController
@RequestMapping("/api/v1/Group")
public class GroupRestController {

    private final GroupService groupService;
    private final AuthService authService;

    public GroupRestController(GroupService groupService, AuthService authService) {
        this.groupService = groupService;
        this.authService = authService;
    }

    @Operation(summary = "Все группы (админ)")
    @GetMapping("all-groups")
    public List<Group> allGroups() {
        return groupService.getAllGroups();
    }

    @Operation(summary = "Группы, в которых преподаватель ведёт дисциплины")
    @GetMapping("supervised-groups")
    public List<Group> supervisedGroups() {
        return groupService.getSupervisedGroupsForTeacher(authService.getCurrentUserId());
    }
}
