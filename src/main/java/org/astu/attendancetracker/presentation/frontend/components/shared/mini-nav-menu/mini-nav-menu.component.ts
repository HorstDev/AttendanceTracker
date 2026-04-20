import { Component, OnInit } from '@angular/core';
import { Subject } from 'src/app/models/subject';
import { DialogChoosingSubjectComponent } from '../dialog-choosing-subject/dialog-choosing-subject.component';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { SubjectService } from 'src/app/services/subject.service';

@Component({
  selector: 'app-mini-nav-menu',
  templateUrl: './mini-nav-menu.component.html',
  styleUrls: ['./mini-nav-menu.component.scss'],
})
export class MiniNavMenuComponent implements OnInit{
  subjects: Subject[] = [];
  selectedSubject: Subject | null = null;

  constructor(private _subjectService: SubjectService, private dialog: MatDialog) { }

  ngOnInit(): void {
    this.getTaughtSubjects();
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

  openDialog(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      subjects: this.subjects, // Передаваемый массив
    };
  
    const dialogRef = this.dialog.open(DialogChoosingSubjectComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      this.selectedSubject = result;
    });
  }
}
