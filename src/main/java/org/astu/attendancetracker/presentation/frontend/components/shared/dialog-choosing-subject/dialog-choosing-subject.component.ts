import { AfterViewInit, Component, Inject} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'src/app/models/subject';
import { SubjectService } from 'src/app/services/subject.service';

@Component({
  selector: 'app-dialog-choosing-subject',
  templateUrl: './dialog-choosing-subject.component.html',
  styleUrls: ['./dialog-choosing-subject.component.scss']
})
export class DialogChoosingSubjectComponent {
  subjects: Subject[] = [];
  selectedSubject: Subject | null = null;

  constructor(
    public dialogRef: MatDialogRef<DialogChoosingSubjectComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {subjects: Subject[]},
  ) {
    this.subjects = data.subjects;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
