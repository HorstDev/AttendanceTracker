package org.astu.attendancetracker.presentation.services.impl;

import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.persistence.repositories.GroupRepository;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Group saveGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        return groupRepository.save(group);
    }
}
