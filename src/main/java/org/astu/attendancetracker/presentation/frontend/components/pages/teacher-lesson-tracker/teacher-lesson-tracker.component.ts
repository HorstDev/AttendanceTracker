import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LessonUserStatusData } from 'src/app/interfaces/lesson-user-status-data';
import { LessonUserStatusWithUserData } from 'src/app/interfaces/lesson-user-status-with-user-data';
import { LessonUserStatusesData } from 'src/app/interfaces/lesson-user-statuses-data';
import { Lesson } from 'src/app/models/lesson';
import { LessonService } from 'src/app/services/lesson.service';

@Component({
  selector: 'app-teacher-lesson-tracker',
  templateUrl: './teacher-lesson-tracker.component.html',
  styleUrls: ['./teacher-lesson-tracker.component.scss']
})
export class TeacherLessonTrackerComponent implements OnInit {
  currentLessons: Lesson[] = [];
  lessonsToStart: Lesson[] = [];
  lessonsInChosenDay: Lesson[] = [];
  lessonsUserStatusesData: LessonUserStatusesData[] = [];
  currentLessonsIsLoading: boolean = true;
  selectedDate?: Date;

  selectedTabIndex: number = 0;
  lessonStatusIds: string[] = [];
  successQrScannedMessage: string = '';

  constructor(private _lessonService: LessonService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.setCurrentLessons();
  }

  setCurrentLessons(): void {
    this.currentLessonsIsLoading = true;

    this._lessonService.getCurrentLessons().subscribe({
      next: (lessonsFromServer: Lesson[]) => {
        // P.S. ... - это клонирование массива. Если не клонировать, то оба массива указывают на один адрес в куче
        this.currentLessons = [...lessonsFromServer];
        this.lessonsToStart = [...lessonsFromServer];
        this.lessonsInChosenDay = [];
        this.selectedDate = undefined;
        // Если какое-то из текущих занятий начато преподавателем и ведется прямо сейчасЮ устанавливаем статусы студентов
        if (this.someLessonIsInProgressNow())
          this.setLessonUserStatusesForLessonsInProgress();
      },
      error: (err) => {
        
      },
      complete: () => {
        this.currentLessonsIsLoading = false;
      }
    });     
  }

  setLessonsInChosenDay(date: Date): void {
    this._lessonService.getLessonsInDay(date).subscribe({
      next: (lessonsFromServer: Lesson[]) => {
        this.lessonsInChosenDay = lessonsFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }

  stopLessons(): void {
    this._lessonService.stopLessons(this.lessonsToStart).subscribe({
      next: (lessonsFromServer: Lesson[]) => {
        this.setCurrentLessons();
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }

  startLessons(): void {
    this._lessonService.startLessons(this.lessonsToStart).subscribe({
      next: (lessonsFromServer: Lesson[]) => {
        this.currentLessons = [...lessonsFromServer];
        this.lessonsToStart = [...lessonsFromServer];
        this.setLessonUserStatusesForLessonsInProgress();
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }

  setLessonUserStatusesForLessonsInProgress(): void {
    this._lessonService.getLessonUserStatusesInProgress().subscribe({
      next: (lessonsUserStatusesFromServer: LessonUserStatusesData[]) => {
        this.lessonsUserStatusesData = lessonsUserStatusesFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }

  updateLessonUserStatuses(): void {
    const lessonStatuses = this.lessonsUserStatusesData.flatMap(x => x.lessonUserStatusesWithUsers.flatMap(y => y.lessonUserStatus));
    this._lessonService.updateLessonStatuses(lessonStatuses).subscribe({
      next: (lessonsStatusesFromServer: LessonUserStatusData[]) => {

        this.openSnackBar('Успешно сохранено', 'Ок');
      },
      error: (err) => {
        this.openSnackBar('Ошибка! Не удалось сохранить данные', 'Ок');
      },
      complete: () => {

      }
    });     
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
    });
  }

  onDateChange(event: any) {
    this.selectedDate = event.value;
    this.setLessonsInChosenDay(this.selectedDate!);
  }

  addLessonToLessonsStart(lesson: Lesson) {
    // Добавить занятие в готовые для старта можем только те, которые не проведены
    if(!this.lessonsToStart.includes(lesson) && !lesson.isStarted) {
      this.lessonsToStart.push(lesson);
    }
  }

  removeLessonFromLessonsStart(lesson: Lesson) {
    var indexToDelete = this.lessonsToStart.indexOf(lesson);
    if (indexToDelete != -1)
      this.lessonsToStart.splice(indexToDelete, 1);
  }

  // Если какое-то занятие из текущих занятий сейчас проводится, то возвращается true
  someLessonIsInProgressNow() : boolean {
    return this.currentLessons.some(lesson => lesson.isStarted);
  }

  // Обновляем статус посещения студентом занятия
  updateVisitStatus(isSelected: boolean, lessonStatus: LessonUserStatusData): void {
    lessonStatus.isVisited = isSelected;
  }

  // Обновление статуса занятия с отправкой на сервер (сделано для того, чтобы сразу при отметке студента производилась отправка)
  // на сервер без дополнительных нажатий кнопок (может быть затратно для сервера)
  updateVisitStatusWithRequestToServer(isSelected: boolean, lessonStatus: LessonUserStatusData): void {
    lessonStatus.isVisited = isSelected;
    const arrayOfStatuses: LessonUserStatusData[] = [lessonStatus]
    this._lessonService.updateLessonStatuses(arrayOfStatuses).subscribe({
      next: (lessonsStatusesFromServer: LessonUserStatusData[]) => {
        
      },
      error: (err) => {
        this.openSnackBar('Ошибка! Не удалось сохранить данные', 'Ок');
      },
      complete: () => {

      }
    });  
  }

  onScanSuccess(result: string) {
    if (this.lessonStatusIds.includes(result)) {
      this.openSnackBar('Вы уже отмечались!', 'Ок');
      return;
    }

    // Добавляем сразу в массив результатов, не дожидаясь ответа с сервера, т.к. может занять время, а студента надо уведомлять сразу
    this.lessonStatusIds.push(result);

    this._lessonService.checkLessonStatusVisited(result).subscribe({
      next: (lessonsStatusFromServer: LessonUserStatusData) => {
        // Отмечаем студента отмеченным в массиве статусов
        const indexMain = this.lessonsUserStatusesData.findIndex(x => x.lessonUserStatusesWithUsers.findIndex(y => y.lessonUserStatus.id == result));
        const indexSecond = this.lessonsUserStatusesData[indexMain].lessonUserStatusesWithUsers.findIndex(x => x.lessonUserStatus.id == result);
        this.lessonsUserStatusesData[indexMain].lessonUserStatusesWithUsers[indexSecond].lessonUserStatus = lessonsStatusFromServer;

        this.successQrScannedMessage = `${this.lessonsUserStatusesData[indexMain].lessonUserStatusesWithUsers[indexSecond].studentName} отмечен!`

        this.openSnackBar('Вы успешно отмечены!', 'Ок');
      },
      error: (err) => {
        this.openSnackBar(err.error.message, 'Ок');
      },
      complete: () => {

      }
    });  
  }

  onTabChange(index: number) {
    this.selectedTabIndex = index;
    if (index === 2) {
      this.lessonStatusIds = [];
      this.successQrScannedMessage = ''
    }
  }

  startedLessonsHasPractice(): boolean {
    const index = this.lessonsUserStatusesData.findIndex(x => x.lesson.type == 'PracticeLesson');
    return index != -1;
  }
}
