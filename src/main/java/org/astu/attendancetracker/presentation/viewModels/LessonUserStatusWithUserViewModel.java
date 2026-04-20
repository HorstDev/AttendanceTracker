package org.astu.attendancetracker.presentation.viewModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonUserStatusWithUserViewModel {
    private String studentName;
    private LessonUserStatusViewModel lessonUserStatus;
}
