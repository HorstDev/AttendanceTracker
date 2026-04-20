import { SubjectInformationReport } from "./subject-information-report";

export interface StudentReport {
    studentName: string;
    subjectsInformationReport: SubjectInformationReport[];
}