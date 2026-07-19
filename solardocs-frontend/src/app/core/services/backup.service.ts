import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class BackupService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/backup`;

  create(): Observable<string> {
    return this.http.post<ApiResponse<string>>(`${this.base}/create`, {}).pipe(map(r => r.data));
  }

  list(): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.base}/list`).pipe(map(r => r.data));
  }

  restore(fileName: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.base}/restore`, { fileName }).pipe(map(() => undefined));
  }
}
