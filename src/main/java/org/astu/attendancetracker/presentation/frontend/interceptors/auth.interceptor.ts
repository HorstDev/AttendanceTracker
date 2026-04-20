import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, catchError, switchMap, throwError } from "rxjs";
import { Router } from "@angular/router";
import { AuthService } from "../services/auth.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  isRefreshed = false;

  constructor(private _authService: AuthService, private _router: Router) {}

  intercept(
      req: HttpRequest<any>,
      next: HttpHandler
  ): Observable<HttpEvent<any>> {
      const token = localStorage.getItem('authToken');

      if (token) {
          req = req.clone({
              setHeaders: {Authorization: `Bearer ${token}`},
          });
      }

      // При истечении срока токена удаляем токен из localStorage и перенаправляем на страницу входа
      return next.handle(req).pipe(
          catchError((error: HttpErrorResponse) => {
            if (error.status === 401) {
              this._authService.logout();
              // // Если ошибку 401 прокинуло обновление refresh токена (он устарел) 
              // if (req.url.includes('/Auth/refresh-token')) {
              //   this._authService.logout();
              //   this._router.navigate(['/login']);
              //   return throwError(() => error);
              // }

              // // Обновляем токен (refresh и access)
              // return this._authService.refreshToken().pipe(
              //   switchMap((newToken: string) => {
              //     localStorage.setItem('authToken', newToken);

              //     const cloneReq = req.clone({
              //       setHeaders: { Authorization: `Bearer ${newToken}` },
              //     });

              //     return next.handle(cloneReq);
              //   }),
              // )
            }
          return throwError(() => error);
          })
        );
  }
}