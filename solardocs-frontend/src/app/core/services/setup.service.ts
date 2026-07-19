import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

export interface SetupStatus {
  configured: boolean;
  currentDataDir: string;
}

@Injectable({ providedIn: 'root' })
export class SetupService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/setup`;

  status(): Observable<SetupStatus> {
    return this.http.get<ApiResponse<SetupStatus>>(`${this.base}/status`).pipe(map(r => r.data));
  }

  chooseDataDirectory(path: string): Observable<string> {
    return this.http.post<ApiResponse<string>>(`${this.base}/data-directory`, { path }).pipe(map(r => r.data));
  }
}
