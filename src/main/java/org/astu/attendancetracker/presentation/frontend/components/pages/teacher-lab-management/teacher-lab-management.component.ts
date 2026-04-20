import { Component } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { Subject } from 'src/app/models/subject';
import { SubjectService } from 'src/app/services/subject.service';
import { DialogChoosingSubjectComponent } from '../../shared/dialog-choosing-subject/dialog-choosing-subject.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-teacher-lab-management',
  templateUrl: './teacher-lab-management.component.html',
  styleUrls: ['./teacher-lab-management.component.scss']
})
export class TeacherLabManagementComponent {
  subjects: Subject[] = [];
  selectedSubject: Subject | null = null;

  selectedTabIndex = 0;

  constructor(private _subjectService: SubjectService, private dialog: MatDialog, private _router: Router) { }

  ngOnInit(): void {
    this.getTaughtSubjects();
  }

  onTabChange(event: any): void {
    switch (event.index) {
      case 0:
        if(this.selectedSubject != null) {
          this._router.navigateByUrl('/lab-management/lab-statuses/' + this.selectedSubject.id);
        }
        break;
      case 1:
        if(this.selectedSubject != null) {
          this._router.navigateByUrl('/lab-management/lab-creation/' + this.selectedSubject.id);
        }
        break;
      default:
        if(this.selectedSubject != null) {
          this._router.navigateByUrl('/lab-management/lab-statuses/' + this.selectedSubject.id);
        }
        break;
    }
  }

  getTaughtSubjects() {
    this._subjectService.getTaugthSubjects().subscribe({
      next: (subjectsFromServer: Subject[]) => {
        this.subjects = subjectsFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });     
  }

  onSubjectChange(subject: Subject): void {
    this.selectedSubject = subject;
    if(this.selectedSubject != null) {
      this._router.navigateByUrl('/lab-management/lab-statuses/' + this.selectedSubject.id);
    }
  }

  openDialog(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      subjects: this.subjects, // Передаваемый массив
    };
  
    const dialogRef = this.dialog.open(DialogChoosingSubjectComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      this.selectedSubject = result;
      if(this.selectedSubject != null) {
        this._router.navigateByUrl('/lab-management/lab-statuses/' + this.selectedSubject.id);
      }
      this.selectedTabIndex = 0;
    });
  }
}
