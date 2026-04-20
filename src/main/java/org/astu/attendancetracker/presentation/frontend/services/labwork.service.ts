import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { LabWork } from '../models/labWork';
import { UserLabWorkStatusesData } from '../interfaces/user-labwork-statuses-data';
import { LabWorkStatusData } from '../interfaces/labwork-status-data';

@Injectable({
  providedIn: 'root'
})
export class LabworkService {

  constructor(private http: HttpClient) { }

  public getLabWorks(subjectId: string) : Observable<LabWork[]> {
    return this.http.get<LabWork[]>(`${environment.apiUrl}/LabWork/${subjectId}/get-many`);
  }

  public createLab(newLab: any) : Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/LabWork`,
    newLab);
  }

  public deleteLab(labWorkId: string) : Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/LabWork/${labWorkId}`);
  }

  public getUserLabWorkStatuses(subjectId: string) : Observable<UserLabWorkStatusesData[]> {
    return this.http.get<UserLabWorkStatusesData[]>(`${environment.apiUrl}/LabWork/${subjectId}/students-labWork-statuses`);
  }

  public changeUserLabWorkStatus(labWorkStatusData: LabWorkStatusData) : Observable<LabWorkStatusData> {
    return this.http.put<LabWorkStatusData>(`${environment.apiUrl}/LabWork/lab-work-status-change-state`,
    labWorkStatusData
    );
  }
}
