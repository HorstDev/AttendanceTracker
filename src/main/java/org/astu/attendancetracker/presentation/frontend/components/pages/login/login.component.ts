import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UserLoginData } from 'src/app/interfaces/user/user-login-data';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  user: UserLoginData = {
    login: '',
    password: '',
  };
  errorMessage: string | null = null;
  isLoading: boolean = false;

  constructor(private _authService: AuthService, private _router: Router, private snackBar: MatSnackBar) {}

  ngOnInit() {
    if (this._authService.loggedIn()) {
      const role = this._authService.getRole();
      if (role == 'ADMIN')
        this._router.navigate(['/group-management']);
      else if (role == 'TEACHER')
        this._router.navigate(['/lesson-tracker']);
      else
        this._router.navigate(['/report-student']);
    }
  }

  register(user: UserLoginData) {
    this._authService.register(user).subscribe({
      next: () => { 
        this.errorMessage = null;
      },
      error: (err) => {
        //Ошибка регистрации
        this.errorMessage = err.error;
       },
    })
  }

  login(user: UserLoginData) {
    this.isLoading = true;

    this._authService.login(user).subscribe({
      next: (response) => {
        
        localStorage.setItem('authToken', response.token);
        if (this._authService.loggedIn()) {
          const role = this._authService.getRole();
          if (role == 'ADMIN')
            this._router.navigate(['/group-management']);
          else if (role == 'TEACHER')
            this._router.navigate(['/lesson-tracker']);
          else
            this._router.navigate(['/report-student']);
        }
      },
      error: (err) => {
        this.openSnackBar('Не удалось войти в систему!', 'Ок')
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    })
  }

  getMe() {
    this._authService.getMe().subscribe({
      next: (name: string) => { 
        this.errorMessage = name;
      },
      error: (err) => {
        this.errorMessage = 'не удалось выполнить метод getMe()';
       },
       complete: () => {

       }
    })
  }

  loggedIn() {
    this._authService.loggedIn();
    // return !!localStorage.getItem('authToken');
  }

  logout() {
    this._authService.logout();
  }

  refresh() {
    this._authService.refreshToken().subscribe((token: string) => {
      localStorage.setItem('authToken', token);
    });
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
    });
  }
}
