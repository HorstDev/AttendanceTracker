import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LabLesson } from 'src/app/models/labLesson';
import { LabWork } from 'src/app/models/labWork';
import { LabworkService } from 'src/app/services/labwork.service';
import { LessonService } from 'src/app/services/lesson.service';

@Component({
  selector: 'app-lab-creation',
  templateUrl: './lab-creation.component.html',
  styleUrls: ['./lab-creation.component.scss']
})
export class LabCreationComponent implements OnInit {
  subjectId: string = '';
  labLessons : LabLesson[] = [];
  labWorks: LabWork[] = [];

  // Для добавления лабораторной работы
  addedLabNumber: number = 0;
  addedLabRaiting: number = 0;
  selectedCheckboxIds: string[] = [];
  disabledCheckboxIds: string[] = [];

  constructor(private _route: ActivatedRoute, private _lessonService: LessonService, private _labWorkService: LabworkService) { }

  ngOnInit(): void {
    this._route.params.subscribe(params => {
      this.subjectId = params['subjectId'];
      this.setLabLessons();
      this.setLabWorks();
    })
  }

  setLabLessons(): void {
    this._lessonService.getLabLessons(this.subjectId).subscribe({
      next: (labLessonsFromServer: LabLesson[]) => {
        // Очищаем массив отключенных и отмеченных чекбоксов на случай, если он был заполнен
        this.disabledCheckboxIds.splice(0, this.disabledCheckboxIds.length);
        this.selectedCheckboxIds.splice(0, this.selectedCheckboxIds.length); // очищаем массив
        // Записываем в неактивные чекбоксы те занятия, которые имеют лабы
        labLessonsFromServer.forEach(labLesson => {
          if (labLesson.hasLabWork)
            this.disabledCheckboxIds.push(labLesson.id);
        });
        this.labLessons = labLessonsFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });     
  }

  setLabWorks(): void {
    this._labWorkService.getLabWorks(this.subjectId).subscribe({
      next: (labWorksFromServer: LabWork[]) => {
        // Записываем в неактивные чекбоксы те занятия, которые имеют лабы
        this.labWorks = labWorksFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });     
  }

  submitAddLabWorkForm() {
    const newLab = {
      number: this.addedLabNumber,
      score: this.addedLabRaiting,
      labLessonsIds: this.selectedCheckboxIds
    }

    this._labWorkService.createLab(newLab).subscribe({
      next: () => {
        // this.disabledCheckboxIds = this.disabledCheckboxIds.concat(this.selectedCheckboxIds);
        // this.selectedCheckboxIds.splice(0, this.selectedCheckboxIds.length); // очищаем массив
        this.setLabLessons();
        this.setLabWorks();
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });    
  }

  deleteLabWork(labWorkId: string) {
    this._labWorkService.deleteLab(labWorkId).subscribe({
      next: () => {
        this.setLabLessons();
        this.setLabWorks();
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });    
  }

  onCheckboxChange(lessonId: string): void {
    const index = this.selectedCheckboxIds.indexOf(lessonId);

    if (index === -1) {
      // Если не найден, добавим id в массив
      this.selectedCheckboxIds.push(lessonId);
    } else {
      // Если найден, удалим id из массива
      this.selectedCheckboxIds.splice(index, 1);
    }
  }

  isCheckboxDisabled(id: string): boolean {
    return this.disabledCheckboxIds.includes(id);
  }

  isCheckboxSelected(id: string): boolean {
    return this.selectedCheckboxIds.includes(id);
  }
}
