import { StudentReport } from "./student-report";

export interface GroupReport {
    allSubjects: string[];
    studentReports: StudentReport[];
}