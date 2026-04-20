import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { Profile, User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  public getUsersBySubstringName(substringName: string) : Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/User/${substringName}`);
  }

  public getTeacherProfilesBySubstringName(substringName: string) : Observable<Profile[]> {
    return this.http.get<Profile[]>(`${environment.apiUrl}/User/teachers-by-substring/${substringName}`);
  }

  public getStudentProfilesByGroupId(groupId: string) : Observable<Profile[]> {
    return this.http.get<Profile[]>(`${environment.apiUrl}/User/students/${groupId}`);
  }

  public getTeacherProfilesByGroupId(groupId: string) : Observable<Profile[]> {
    return this.http.get<Profile[]>(`${environment.apiUrl}/User/teachers/${groupId}`);
  }

  public getCuratorProfileByGroupId(groupId: string) : Observable<Profile> {
    return this.http.get<Profile>(`${environment.apiUrl}/User/curator/${groupId}`);
  }

  public addCuratorToGroup(teacherId: string, groupId: string) : Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/User/add-curator/${teacherId}/${groupId}`, { });
  }

  public addStudentToGroup(studentName: string, groupId: string) : Observable<Profile> {
    return this.http.post<Profile>(`${environment.apiUrl}/User/add-student/${studentName}/${groupId}`, { });
  }

  public makeUserAnAdmin(userId: string) : Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/User/make-user-admin/${userId}`, { });
  }

  public makeUserNoAdmin(userId: string) : Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/User/make-user-no-admin/${userId}`, { });
  }
}
