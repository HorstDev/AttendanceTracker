import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-nav-menu',
  templateUrl: './nav-menu.component.html',
  styleUrls: ['./nav-menu.component.scss']
})
export class NavMenuComponent implements OnInit {
  isNavMenuCollapsed: boolean = true;
  userLogin: string | null = '';
  userRoles: string | null = '';

  constructor(public _authService: AuthService) {}

  ngOnInit(): void {
    this.userLogin = this._authService.getLogin();
    this.userRoles = this._authService.getRole();
  }

  get navMenuCssClass() : string {
    return this.isNavMenuCollapsed ? 'collapse' : ''
  }

  toggleMenu() {
    this.isNavMenuCollapsed = !this.isNavMenuCollapsed;
  }
}
