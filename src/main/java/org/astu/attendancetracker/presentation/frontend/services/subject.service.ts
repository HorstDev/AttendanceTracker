import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Subject } from '../models/subject';
import { environment } from 'src/environments/environment.development';
import { SubjectMapping } from '../models/subject-mapping';

@Injectable({
  providedIn: 'root'
})
export class SubjectService {

  constructor(private http: HttpClient) { }

  public getTaugthSubjects() : Observable<Subject[]> {
    return this.http.get<Subject[]>(`${environment.apiUrl}/Subject/taught-subjects`);
  }

  public getSubjectsByGroupId(groupId: string) : Observable<Subject[]> {
    return this.http.get<Subject[]>(`${environment.apiUrl}/Subject/subjects/${groupId}`);
  }

  public getAllSubjectMappings() : Observable<SubjectMapping[]> {
    return this.http.get<SubjectMapping[]>(`${environment.apiUrl}/Subject/subject-mappings`);
  }

  public addSubjectMapping(subjectMapping: any) : Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/Subject/subject-mapping`, subjectMapping);
  }

  public updateSubjectMapping(subjectMapping: SubjectMapping) : Observable<SubjectMapping> {
    return this.http.put<SubjectMapping>(`${environment.apiUrl}/Subject/subject-mapping`, subjectMapping);
  }

  public deleteSubjectMapping(subjectMappingId: string) : Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/Subject/subject-mapping/${subjectMappingId}`);
  }
}
