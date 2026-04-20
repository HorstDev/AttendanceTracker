import { Component, OnInit } from '@angular/core';
import { GroupReport } from 'src/app/interfaces/report/group-report';
import { Group } from 'src/app/models/group';
import { AuthService } from 'src/app/services/auth.service';
import { GroupService } from 'src/app/services/group.service';
import { ReportService } from 'src/app/services/report.service';

@Component({
  selector: 'app-report-group',
  templateUrl: './report-group.component.html',
  styleUrls: ['./report-group.component.scss']
})
export class ReportGroupComponent implements OnInit {
  groups: Group[] = []
  selectedGroup?: Group;
  report?: GroupReport;
  selectedScore: number = -1;
  selectedNotVisitedCount: number = 10000;

  userRoles: string | null = '';

  constructor(private groupService: GroupService, private reportService: ReportService, private authService: AuthService) { }

  ngOnInit(): void {
    this.userRoles = this.authService.getRole();
    if (this.userRoles && this.userRoles.includes('ADMIN'))
      this.setAllGroups();
    else
      this.setSupervisedGroup();
  }

  setAllGroups(): void {
    this.groupService.getAllGroups().subscribe({
      next: (groupsFromServer: Group[]) => {
        this.groups = groupsFromServer;
        // this.setReportForGroup(groupFromServer.id);
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }

  setSupervisedGroup(): void {
    this.groupService.getSupervisedGroups().subscribe({
      next: (groupsFromServer: Group[]) => {
        this.groups = groupsFromServer;
        // this.setReportForGroup(groupFromServer.id);
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }

  onGroupChange(group: Group) {
    this.selectedGroup = group;
    this.setReportForGroup(this.selectedGroup.id);
  }

  setReportForGroup(groupId: string): void {
    this.reportService.getReportForGroup(groupId).subscribe({
      next: (reportFromServer: GroupReport) => {
        this.report = reportFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {

      }
    });     
  }
}
