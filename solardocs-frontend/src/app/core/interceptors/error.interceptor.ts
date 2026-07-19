import { Injectable, inject } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private snackBar = inject(MatSnackBar);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An error occurred. Please try again later.';

        if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = `Error: ${error.error.message}`;
        } else {
          // Server-side error
          if (error.status === 0) {
            errorMessage = 'Unable to connect to server. Please check your connection.';
          } else if (error.status === 400) {
            errorMessage = error.error?.error?.message || 'Invalid request. Please check your input.';
          } else if (error.status === 404) {
            errorMessage = 'Requested resource not found.';
          } else if (error.status === 500) {
            errorMessage = 'Server error. Please contact support.';
          } else if (error.status === 503) {
            errorMessage = 'Server is temporarily unavailable. Please try again later.';
          } else {
            errorMessage = `Error: ${error.status} - ${error.statusText}`;
          }
        }

        // Log error in development
        console.error('HTTP Error:', error);

        // Show snackbar notification
        this.snackBar.open(errorMessage, 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar'],
          verticalPosition: 'top'
        });

        return throwError(() => error);
      })
    );
  }
}
