import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { LabLesson } from '../models/labLesson';
import { Lesson } from '../models/lesson';
import { DatePipe } from '@angular/common';
import { LessonUserStatusesData } from '../interfaces/lesson-user-statuses-data';
import { LessonUserStatusData } from '../interfaces/lesson-user-status-data';

@Injectable({
  providedIn: 'root'
})
export class LessonService {

  constructor(private http: HttpClient, private datePipe: DatePipe) { }

  public getLabLessons(subjectId : string) : Observable<LabLesson[]> {
    return this.http.get<LabLesson[]>(`${environment.apiUrl}/Lesson/${subjectId}/lab-lessons`);
  }

  public getCurrentLessons() : Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${environment.apiUrl}/lesson/current-lessons`)
  }

  public getCurrentLessonStatusForStudent() : Observable<LessonUserStatusData> {
    return this.http.get<LessonUserStatusData>(`${environment.apiUrl}/lesson/active-lesson-status`);
  }

  public getLessonUserStatusesInProgress() : Observable<LessonUserStatusesData[]> {
    return this.http.get<LessonUserStatusesData[]>(`${environment.apiUrl}/lesson/lessons-in-progress-user-statuses`);
  }

  public getLessonsInDay(date: Date) : Observable<Lesson[]> {
    const params = new HttpParams()
        .set('date', this.datePipe.transform(date, 'yyyy-MM-dd')!);
    
    return this.http.get<Lesson[]>(
        `${environment.apiUrl}/lesson/lessons-in-day`,
        { params }
    );
  }

  public startLessons(lessons: Lesson[]) : Observable<Lesson[]> {
    const ids = lessons.map(lesson => lesson.id); 
    return this.http.put<Lesson[]>(`${environment.apiUrl}/lesson/start-lessons`,
    ids
    );
  }

  public stopLessons(lessons: Lesson[]) : Observable<Lesson[]> {
    const ids = lessons.map(lesson => lesson.id);
    return this.http.put<Lesson[]>(`${environment.apiUrl}/lesson/stop-lessons`, ids);
  }

  public updateLessonStatuses(lessonStatuses: LessonUserStatusData[]) : Observable<LessonUserStatusData[]> {
    return this.http.put<LessonUserStatusData[]>(`${environment.apiUrl}/lesson/update-lesson-statuses`, lessonStatuses);
  }

  public checkLessonStatusVisited(lessonStatusId: string) : Observable<LessonUserStatusData> {
    return this.http.put<LessonUserStatusData>(`${environment.apiUrl}/lesson/check-lesson-status-visited/${lessonStatusId}`, {});
  }
}
