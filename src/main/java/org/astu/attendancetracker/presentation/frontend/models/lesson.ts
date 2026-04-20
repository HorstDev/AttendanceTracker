export class Lesson {
    id: string = '';
    subjectName: string = '';
    groupName: string = '';
    start?: Date;
    end?: Date;
    realStart?: Date;
    realEnd?: Date;
    type: string = '';
    isStarted: boolean = false;
}