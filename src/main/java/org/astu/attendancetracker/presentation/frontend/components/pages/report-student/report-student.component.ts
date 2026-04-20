import { Component, OnInit } from '@angular/core';
import { LabWorkStatusData } from 'src/app/interfaces/labwork-status-data';
import { StudentSubjectReport } from 'src/app/interfaces/report/student-subject-report';
import { ReportService } from 'src/app/services/report.service';

@Component({
  selector: 'app-report-student',
  templateUrl: './report-student.component.html',
  styleUrls: ['./report-student.component.scss']
})
export class ReportStudentComponent implements OnInit {
  report: StudentSubjectReport[] = [];
  currentDate: Date = new Date();

  constructor(private reportService: ReportService) { }

  ngOnInit(): void {
    this.setReport();
  }

  setReport(): void {
    this.reportService.getReportForStudent().subscribe({
      next: (reportFromServer: StudentSubjectReport[]) => {
        this.report = reportFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }

  lengthOfDoneLabWorks(labWorkStatuses: LabWorkStatusData[]): number {
    return labWorkStatuses.filter(x => x.isDone).length;
  }
  // Возвращает true, если студент укладывается в срок, иначе false
  meetsDeadline(labWorkStatuses: LabWorkStatusData[], labWorkNumberShouldDone: number): boolean {
    return this.lengthOfDoneLabWorks(labWorkStatuses) >= labWorkNumberShouldDone;
  }
}
