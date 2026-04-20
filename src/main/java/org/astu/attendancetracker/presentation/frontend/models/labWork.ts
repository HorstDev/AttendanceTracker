import { LabLesson } from "./labLesson";

export class LabWork {
    id: string = '';
    number: number = 0;
    score: number = 0;
    lessons: LabLesson[] = [];
}