package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.domain.Group;

public interface GroupService {

    Group saveGroup(String groupName);
}
