import { Lesson } from "../models/lesson";
import { LessonUserStatusWithUserData } from "./lesson-user-status-with-user-data";

export interface LessonUserStatusesData {
    lesson: Lesson;
    lessonUserStatusesWithUsers: LessonUserStatusWithUserData[];
}