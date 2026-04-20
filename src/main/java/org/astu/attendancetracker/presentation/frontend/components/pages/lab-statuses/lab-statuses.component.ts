import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LabWorkStatusData } from 'src/app/interfaces/labwork-status-data';
import { UserLabWorkStatusesData } from 'src/app/interfaces/user-labwork-statuses-data';
import { LabworkService } from 'src/app/services/labwork.service';

@Component({
  selector: 'app-lab-statuses',
  templateUrl: './lab-statuses.component.html',
  styleUrls: ['./lab-statuses.component.scss']
})
export class LabStatusesComponent implements OnInit {
  subjectId: string = '';
  usersLabWorkStatusesData: UserLabWorkStatusesData[] = [];

  constructor(private _route: ActivatedRoute, private _labWorkService: LabworkService) { }

  ngOnInit(): void {
    this._route.params.subscribe(params => {
      this.subjectId = params['subjectId'];
      this.setUserLabWorkStatuses();
    })
  }

  setUserLabWorkStatuses(): void {
    this._labWorkService.getUserLabWorkStatuses(this.subjectId).subscribe({
      next: (userLabWorkStatusesFromServer: UserLabWorkStatusesData[]) => {
        this.usersLabWorkStatusesData = userLabWorkStatusesFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });     
  }

  changeUserLabWorkStatus(userLabWorkStatusId: string): void {
    // Находим нужный labWorkStatus
    for(const userLabWorkStatusesData of this.usersLabWorkStatusesData) {
      for(const labWorkStatus of userLabWorkStatusesData.labWorkUserStatuses) {
        if(labWorkStatus.id == userLabWorkStatusId) {
          // Обновляем найденный
          this._labWorkService.changeUserLabWorkStatus(labWorkStatus).subscribe({
            next: (labWorkStatusDataFromServer: LabWorkStatusData) => {
              // Обновляем объект по ссылке
              Object.assign(labWorkStatus, labWorkStatusDataFromServer);
            },
            error: (err) => {
              
            },
            complete: () => {
              // Выходим из функции, дальше не проходимся
              return;
            }
          });     

        }
      }
    }
  }
}
