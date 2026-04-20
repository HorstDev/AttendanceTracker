import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { Group } from '../models/group';

@Injectable({
  providedIn: 'root'
})
export class GroupService {

  constructor(private http: HttpClient) { }

  public getAllGroups() : Observable<Group[]> {
    return this.http.get<Group[]>(`${environment.apiUrl}/Group/all-groups`);
  }

  public getFiveGroupsBySubstring(substring: string) : Observable<Group[]> {
    return this.http.get<Group[]>(`${environment.apiUrl}/Group/groups-by-substring/${substring}`);
  }

  public getFiveGroupsFromApiTableBySubstring(substring: string) : Observable<Group[]> {
    return this.http.get<Group[]>(`${environment.apiUrl}/Group/groups-by-substring-from-api-table/${substring}`);
  }

  public getGroupById(id: string) : Observable<Group> {
    return this.http.get<Group>(`${environment.apiUrl}/Group/${id}`);
  }

  public getSupervisedGroups() : Observable<Group[]> {
    return this.http.get<Group[]>(`${environment.apiUrl}/Group/supervised-groups`);
  }

  public deleteGroup(id: string) : Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/Group/${id}`);
  }

  public createGroup(groupName: string, curriculumFile: File) : Observable<any> {
    const formData = new FormData();
    formData.append('excelCurriculum', curriculumFile)

    // Без кодировки groupName запрос не отправится, т.к. в groupName есть /
    const encodedGroupName = encodeURIComponent(groupName);

    return this.http.post<any>(`${environment.apiUrl}/Group/${encodedGroupName}`,
    formData);
  }

  public uploadDependenciesForGroup(groupId: string, semesterStart: Date) : Observable<any> {
    const formattedDate = this.formatDate(semesterStart); // Преобразуем дату в формат YYYY-MM-DD
    return this.http.post<any>(`${environment.apiUrl}/Group/${groupId}/upload-dependencies/${formattedDate}`, { });
  }

  public uploadDependenciesForGroupFromJSONApiTable(groupId: string, semesterStart: Date, jsonFromApiTable: File) : Observable<any> {
    const formData = new FormData();
    formData.append('jsonFile', jsonFromApiTable)
    const formattedDate = this.formatDate(semesterStart); // Преобразуем дату в формат YYYY-MM-DD
    return this.http.post<any>(`${environment.apiUrl}/Group/${groupId}/upload-dependencies-from-file/${formattedDate}`,
    formData
    );
  }

  // Форматирование даты, чтобы оно было выбранным и не изменялось из-за часового пояса
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // месяцы с 0 до 11
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
}
}
