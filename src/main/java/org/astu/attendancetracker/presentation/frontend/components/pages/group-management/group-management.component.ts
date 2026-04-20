import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Observable, catchError, debounceTime, map, of, startWith, switchMap  } from 'rxjs';
import { Group } from 'src/app/models/group';
import { GroupService } from 'src/app/services/group.service';

@Component({
  selector: 'app-group-management',
  templateUrl: './group-management.component.html',
  styleUrls: ['./group-management.component.scss']
})
export class GroupManagementComponent implements OnInit {
  @ViewChild('dialogTemplate') dialogTemplate!: TemplateRef<any>;
  @ViewChild('dialogDeleteTemplate') dialogDeleteTemplate!: TemplateRef<any>;
  dialogRef!: MatDialogRef<any>;

  groupNameToAdd: string | null = null;
  curriculumFileToAdd: File | null = null;

  groups: Group[] = [];
  selectedDate: Date | null = null;
  selectedGroupId: string = '';
  currentDate: Date = new Date();

  // Autocomplete
  myControl = new FormControl('');
  groupsFromApiTable: Group[] = [];
  filteredGroupsFromApiTable?: Observable<Group[]>;

  constructor(private snackBar: MatSnackBar, private _groupService: GroupService, private dialog: MatDialog, private _router: Router) {}

  ngOnInit(): void {
    this.setGroups();

    this.filteredGroupsFromApiTable = this.myControl.valueChanges.pipe(
      startWith(''),
      debounceTime(500), // Ожидание ввода в течение 2 секунд
      switchMap(value => this.setGroupsFromApiTable(value || '')), // Вызов метода для запроса на сервер и получения нового списка
    );

    // Обработка изменений в поле ввода
    this.myControl.valueChanges.subscribe(value => {
      this.groupNameToAdd = value; // Обновляем значение при ручном вводе
    });
  }

  setGroupsFromApiTable(value: string): Observable<Group[]> {
    return this._groupService.getFiveGroupsFromApiTableBySubstring(value).pipe(
      map((teacherProfilesFromServer: Group[]) => {
        return teacherProfilesFromServer;
      }),
      catchError((err) => {
        console.error('Error fetching teacher profiles:', err);
        return of([]); // Возвращаем пустой массив в случае ошибки
      })
    );
  }

  onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    this.groupNameToAdd = event.option.value.name;
  }

  displayFn(group: Group): string {
    return group ? group.name : '';
  }


  onFileSelected(event: any) {
    this.curriculumFileToAdd = event.target.files[0];
  }

  onDateChange(event: any) {
    this.selectedDate = event.value;
  }

  addNewGroup() {
    if (this.groupNameToAdd && this.curriculumFileToAdd) {
      this._groupService.createGroup(this.groupNameToAdd, this.curriculumFileToAdd).subscribe({
        next: () => {
          this.openSnackBar('Успешно добавлено!', 'Ок');
          this.setGroups()
        },
        error: (err) => {
          this.openSnackBar('Ошибка при добавлении!', 'Ок');
        },
        complete: () => {
          
        }
      });     
    }
    else {
      this.openSnackBar('Ошибка! Не все поля заполнены!', 'Ок');
    }
  }

  setGroups() {
    this._groupService.getAllGroups().subscribe({
      next: (groupsFromServer: Group[]) => {
        this.groups = groupsFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });     
  }

  deleteGroup(groupId: string) {
    this.dialogRef = this.dialog.open(this.dialogDeleteTemplate, {
      width: '250px',
      data: {  }
    });

    this.dialogRef.afterClosed().subscribe(result => {
      if(result === 'yes') {
        this._groupService.deleteGroup(groupId).subscribe({
          next: () => {
            this.setGroups();
          },
          error: (err) => {
            this.openSnackBar('Ошибка при удалении', 'Ок');
          },
          complete: () => {
            
          }
        });  
      }
    });   
  }

  uploadDependenciesForSelectedGroup() {
    if (this.selectedDate == null) {
      this.openSnackBar('Ошибка! Не выбрана дата!', 'Ок');
    }
    else {
      this.dialog.closeAll();

      this._groupService.uploadDependenciesForGroup(this.selectedGroupId, this.selectedDate).subscribe({
        next: () => {
          this.openSnackBar('Успешно загружено!', 'Ок');
          this.setGroups();
        },
        error: (err) => {
          this.openSnackBar(err.error.message, 'Ок');
        },
        complete: () => {
          
        }
      }); 
    }
  }

  navigateToAboutGroupPage(groupId: string) {
    this._router.navigate(['/group-management/about-group/' + groupId]);
  }

  openDialog(groupId: string): void {
    this.selectedGroupId = groupId;

    const dialogRef = this.dialog.open(this.dialogTemplate, {
      width: '250px',
      data: {  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      // Обработка закрытия диалога
    });
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
    });
  }

  onYesClick() {
    this.dialogRef.close('yes');
  }

  onNoClick() {
    this.dialogRef.close('no');
  }

  dateIsExists(dateString?: string): boolean {
    if (!dateString) {
      return false; // Если строка пустая или неопределена, возвращаем false
    }
  
    const date = new Date(dateString);
    return date.getFullYear() !== 1;
  }
}
