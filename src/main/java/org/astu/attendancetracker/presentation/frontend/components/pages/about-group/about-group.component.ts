import { Clipboard } from '@angular/cdk/clipboard';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, debounceTime, map, of, startWith, switchMap } from 'rxjs';
import { Group } from 'src/app/models/group';
import { Subject } from 'src/app/models/subject';
import { Profile } from 'src/app/models/user';
import { AuthService } from 'src/app/services/auth.service';
import { GroupService } from 'src/app/services/group.service';
import { SubjectService } from 'src/app/services/subject.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-about-group',
  templateUrl: './about-group.component.html',
  styleUrls: ['./about-group.component.scss']
})
export class AboutGroupComponent implements OnInit {
  @ViewChild('dialogTemplate') dialogTemplate!: TemplateRef<any>;
  @ViewChild('dialogTemplateJson') dialogTemplateJson!: TemplateRef<any>;
  @ViewChild('dialogDeleteTemplate') dialogDeleteTemplate!: TemplateRef<any>;
  dialogRef!: MatDialogRef<any>;
  
  // Autocomplete
  myControl = new FormControl('');
  curatorProfiles: Profile[] = [];
  filteredCuratorProfiles?: Observable<Profile[]>;

  // Other
  groupId: string = '';
  group: Group | null = null;
  studentNameToAdd: string = '';
  students: Profile[] = [];
  teachers: Profile[] = [];
  curator: Profile | null = null;
  subjects: Subject[] = [];

  selectedDate: Date | null = null;
  jsonFileFromApiTable: File | null = null;

  constructor(private _route: ActivatedRoute, private _userService: UserService,
    private _groupService: GroupService, private snackBar: MatSnackBar, private _authService: AuthService,
    private _subjectService: SubjectService, private dialog: MatDialog, private clipboard: Clipboard
  ) { }

  ngOnInit(): void {
    this._route.params.subscribe(params => {
      this.groupId = params['groupId'];
      this.setGroup();
      this.setStudentProfiles();
      this.setTeacherProfiles();
      this.setCuratorProfile();
      this.setSubjects();
    });

    this.filteredCuratorProfiles = this.myControl.valueChanges.pipe(
      startWith(''),
      debounceTime(500), // Ожидание ввода в течение 2 секунд
      switchMap(value => this.setCuratorProfiles(value || '')), // Вызов метода для запроса на сервер и получения нового списка
    );
  }

  setCuratorProfiles(value: string): Observable<Profile[]> {
    return this._userService.getTeacherProfilesBySubstringName(value).pipe(
      map((teacherProfilesFromServer: Profile[]) => {
        return teacherProfilesFromServer;
      }),
      catchError((err) => {
        console.error('Error fetching teacher profiles:', err);
        return of([]); // Возвращаем пустой массив в случае ошибки
      })
    );
  }

  onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    const selectedTeacherId = event.option.value.id;
    console.log(selectedTeacherId);
    this.addCuratorToGroup(selectedTeacherId);
  }

  displayFn(profile: Profile): string {
    return profile ? profile.name : '';
  }

  setGroup() {
    this._groupService.getGroupById(this.groupId).subscribe({
      next: (groupFromServer: Group) => {
        this.group = groupFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });  
  } 

  setStudentProfiles() {
    this._userService.getStudentProfilesByGroupId(this.groupId).subscribe({
      next: (studentProfilesFromServer: Profile[]) => {
        this.students = studentProfilesFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });  
  }

  setTeacherProfiles() {
    this._userService.getTeacherProfilesByGroupId(this.groupId).subscribe({
      next: (teacherProfilesFromServer: Profile[]) => {
        this.teachers = teacherProfilesFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });  
  }

  setSubjects() {
    this._subjectService.getSubjectsByGroupId(this.groupId).subscribe({
      next: (subjectsFromServer: Subject[]) => {
        this.subjects = subjectsFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });  
  }

  setCuratorProfile() {
    this._userService.getCuratorProfileByGroupId(this.groupId).subscribe({
      next: (curatorFromServer: Profile) => {
        this.curator = curatorFromServer;
      },
      error: (err) => {
        this.curator = null;
      },
      complete: () => {
        
      }
    });  
  }

  addCuratorToGroup(teacherId: string) {
    this._userService.addCuratorToGroup(teacherId, this.groupId).subscribe({
      next: () => {
        this.setCuratorProfile();
      },
      error: (err) => {
        this.openSnackBar(err.error.message, 'Ок');
      },
      complete: () => {
        
      }
    });  
  }

  addStudentToGroup() {
    if (this.studentNameToAdd === '' || !this.studentNameToAdd) {
      this.openSnackBar('Имя не должно быть пустым!', 'Ок');
      return;
    }
    this._userService.addStudentToGroup(this.studentNameToAdd, this.groupId).subscribe({
      next: (studentProfileFromServer: Profile) => {
        this.setStudentProfiles();
        this.openSnackBar('Студент успешно добавлен', 'Ок');
        this.studentNameToAdd = '';
      },
      error: (err) => {
        this.openSnackBar('Ошибка при добавлении', 'Ок');
      },
      complete: () => {
        
      }
    });  
  }

  removeUser(userId: string) {
    this.dialogRef = this.dialog.open(this.dialogDeleteTemplate, {
      width: '250px',
      data: {  }
    });

    this.dialogRef.afterClosed().subscribe(result => {
      if(result === 'yes') {
        this._authService.removeUser(userId).subscribe({
          next: () => {
            this.setStudentProfiles();
          },
          error: (err) => {
            this.openSnackBar('Ошибка при удалении!', 'Ок');
          },
          complete: () => {
            
          }
        }); 
      }
    }); 
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
    });
  }

  onDateChange(event: any) {
    this.selectedDate = event.value;
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(this.dialogTemplate, {
      width: '250px',
      data: {  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      // Обработка закрытия диалога
    });
  }

  uploadDependenciesForSelectedGroup() {
    if (this.selectedDate == null) {
      this.openSnackBar('Ошибка! Не выбрана дата!', 'Ок');
    }
    else {
      this.dialog.closeAll();

      this._groupService.uploadDependenciesForGroup(this.groupId, this.selectedDate).subscribe({
        next: () => {
          this.openSnackBar('Успешно загружено!', 'Ок');
          this.setSubjects();
          this.setTeacherProfiles();
          this.setGroup();
        },
        error: (err) => {
          this.openSnackBar(err.error.message, 'Ок');
        },
        complete: () => {
          
        }
      }); 
    }
  }

  onFileSelected(event: any) {
    this.jsonFileFromApiTable = event.target.files[0];
  }

  openDialogWithJson(): void {
    const dialogRef = this.dialog.open(this.dialogTemplateJson, {
      width: '350px',
      data: {  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      // Обработка закрытия диалога
    });
  }

  uploadDependenciesForSelectedGroupWithJson() {
    if (this.selectedDate == null || this.jsonFileFromApiTable == null) {
      this.openSnackBar('Ошибка! Недостаточно данных!', 'Ок');
    }
    else {
      this.dialog.closeAll();

      this._groupService.uploadDependenciesForGroupFromJSONApiTable(this.groupId, this.selectedDate, this.jsonFileFromApiTable).subscribe({
        next: () => {
          this.openSnackBar('Успешно загружено!', 'Ок');
          this.setSubjects();
          this.setTeacherProfiles();
          this.setGroup();
        },
        error: (err) => {
          this.openSnackBar(err.error.message, 'Ок');
        },
        complete: () => {
          
        }
      }); 
    }
  }

  setPasswordResetLink(userId: string) {
    this._authService.getAuthTokenFor48Hours(userId).subscribe({
      next: (authToken: string) => {
        const relativeUrl = '/changing-account-data';
        const passwordResetLinkForSelectedUser = `${window.location.origin}${relativeUrl}?token=${authToken}`;
        this.clipboard.copy(passwordResetLinkForSelectedUser);
        this.openSnackBar('Ссылка для смены пароля скопирована в буфер обмена!', 'Ок');
      },
      error: (err) => {
        this.openSnackBar('Ошибка. Токен получить не удалось', 'Ок');
      },
      complete: () => {
        
      }
    }); 
  }

  onYesClick() {
    this.dialogRef.close('yes');
  }

  onNoClick() {
    this.dialogRef.close('no');
  }
}
