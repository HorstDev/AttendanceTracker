import { LabWorkStatusData } from "../labwork-status-data";

export interface StudentSubjectReport {
    subjectName: string;
    score: number;
    notVisitedLessonCount: number;
    startedLessonCount: number;
    labWorkUserStatuses: LabWorkStatusData[];
    labWorkNumberShouldDone: number;
    labWorksShouldExist: boolean
}