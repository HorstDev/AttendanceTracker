import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { UserLoginData } from 'src/app/interfaces/user/user-login-data';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-changing-account-data',
  templateUrl: './changing-account-data.component.html',
  styleUrls: ['./changing-account-data.component.scss']
})
export class ChangingAccountDataComponent implements OnInit {
  token: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string | null = null;
  isChanged: boolean = false;

  constructor(private _route: ActivatedRoute, private snackBar: MatSnackBar, private _authService: AuthService, private _router: Router) { }

  ngOnInit(): void {
    this._route.queryParams.subscribe(params => {
      this.token = params['token'];
    });
  }

  changeAccountData() {
    if (this.password != this.confirmPassword) {
      this.openSnackBar('Ошибка! Пароли не совпадают!', 'Ок');
      return;
    }

    const user: UserLoginData = {
      login: this.email,
      password: this.password
    }
    
    this._authService.changeAccountData(this.token, user).subscribe({
      next: () => {
        this.isChanged = true;
      },
      error: (err) => {
        this.openSnackBar(err.error.message, 'Ок');
      },
      complete: () => {
        
      }
    });     
  }

  goToLoginPage() {
    this._router.navigate(['/login']);
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
    });
  }

  isLoggedIn(): boolean {
    return this._authService.loggedIn();
  }
}
