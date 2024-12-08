package org.astu.attendancetracker;

import org.astu.attendancetracker.core.domain.Group;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class GroupTests {

    @ParameterizedTest
    @CsvSource({
            "ДИПРБ-11/1, ДИПРБ-21/1",
            "ДИНРМ-21/1, ДИНРМ-31/1",
            "ДИБСС-31/2, ДИБСС-41/2"
    })
    public void increaseGroupCourseTest(String oldGroupName, String newGroupName) {
        Group group = new Group();
        group.setName(oldGroupName);
        group.increaseCourse();
        assertEquals(group.getName(), newGroupName,
                "Увеличение курса для " + oldGroupName + " некорректное");
    }

    @ParameterizedTest
    @CsvSource({
            "ДИПРБ-11/1, 1",
            "ДИНРМ-21/1, 2",
            "ДИБСС-31/2, 3"
    })
    public void groupCourseTest(String groupName, int course) {
        Group group = new Group();
        group.setName(groupName);
        assertEquals(group.course(), course,
                "У группы + " + groupName + " курс рассчитан неправильно");
    }

    @ParameterizedTest
    @CsvSource({
            "ДИПРБ-11/1, false",
            "ДИНРМ-31/1, true",
            "ДИБСС-51/2, false",
            "ДИБСС-61/2, true",
            "ДКНО-51/2, true",
    })
    public void studyGroupIsOverTest(String groupName, boolean isOver) {
        Group group = new Group();
        group.setName(groupName);
        assertEquals(group.studyIsOver(), isOver, "Некорректно для " + groupName);
    }
}
