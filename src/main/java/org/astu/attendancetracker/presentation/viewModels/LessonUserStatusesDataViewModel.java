package org.astu.attendancetracker.presentation.viewModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonUserStatusesDataViewModel {
    private LessonViewModel lesson;
    private List<LessonUserStatusWithUserViewModel> lessonUserStatusesWithUsers;
}
