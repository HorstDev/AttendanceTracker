import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { GroupReport } from '../interfaces/report/group-report';
import { environment } from 'src/environments/environment.development';
import { Observable } from 'rxjs';
import { StudentSubjectReport } from '../interfaces/report/student-subject-report';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  constructor(private http: HttpClient) { }

  public getReportForGroup(groupId: string) : Observable<GroupReport> {
    return this.http.get<GroupReport>(`${environment.apiUrl}/Report/${groupId}`);
  }

  public getReportForStudent() : Observable<StudentSubjectReport[]> {
    return this.http.get<StudentSubjectReport[]>(`${environment.apiUrl}/Report/student-report`);
  }
}
