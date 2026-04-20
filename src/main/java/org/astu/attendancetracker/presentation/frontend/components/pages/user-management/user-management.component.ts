import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from 'src/app/models/user';
import { AuthService } from 'src/app/services/auth.service';
import { UserService } from 'src/app/services/user.service';
import { Clipboard } from '@angular/cdk/clipboard';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  substringName: string = '';
  users: User[] = [];
  selectedUser?: User;
  passwordResetLinkForSelectedUser: string | null = null;

  constructor(private _userService: UserService, private _authService: AuthService, 
    private snackBar: MatSnackBar, private _clipboard: Clipboard) { }

  ngOnInit(): void {
    this.setUsersBySubstringName();
  }

  setUsersBySubstringName() {
    this._userService.getUsersBySubstringName(this.substringName).subscribe({
      next: (usersFromServer: User[]) => {
        this.users = usersFromServer;
      },
      error: (err) => {
        
      },
      complete: () => {
        
      }
    });     
  }

  setPasswordResetLink() {
    if(this.selectedUser) {
      this._authService.getAuthTokenFor48Hours(this.selectedUser.id).subscribe({
        next: (authToken: string) => {
          const relativeUrl = '/changing-account-data';
          this.passwordResetLinkForSelectedUser = `${window.location.origin}${relativeUrl}?token=${authToken}`;
          this._clipboard.copy(this.passwordResetLinkForSelectedUser);
          this.openSnackBar('Ссылка скопирована в буфер обмена!', 'Ок');
        },
        error: (err) => {
          this.openSnackBar('Ошибка. Токен получить не удалось', 'Ок');
        },
        complete: () => {
          
        }
      }); 
    }
    else {
      this.openSnackBar('Ошибка. Выберите пользователя!', 'Ок');
    }   
  }

  makeSelectedUserAnAdmin() {
    if (this.selectedUser) {
      this._userService.makeUserAnAdmin(this.selectedUser.id).subscribe({
        next: (userFromServer: User) => {
          this.selectedUser = userFromServer;
        },
        error: (err) => {
          
        },
        complete: () => {
          
        }
      });
    }     
  }

  makeSelectedUserNoAdmin() {
    if (this.selectedUser) {
      this._userService.makeUserNoAdmin(this.selectedUser.id).subscribe({
        next: (userFromServer: User) => {
          this.selectedUser = userFromServer;
        },
        error: (err) => {
          
        },
        complete: () => {
          
        }
      });
    }     
  }

  setUserAsSelected(user: User) {
    this.selectedUser = user;
    this.passwordResetLinkForSelectedUser = null;
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
    });
  }

  selectedUserIsAdmin(): boolean {
    return this.selectedUser?.roles.findIndex(x => x.name === 'Admin') != -1;
  }

  selectedUserIsTeacher(): boolean {
    return this.selectedUser?.roles.findIndex(x => x.name === 'Teacher') != -1;
  }
}
