import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

export interface License {
  key: string | null;
  machineFingerprint: string;
  activatedAt: string | null;
  status: 'ACTIVE' | 'NOT_ACTIVATED' | 'EXPIRED_OR_INVALID';
}

@Injectable({ providedIn: 'root' })
export class LicenseService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/license`;

  status(): Observable<License> {
    return this.http.get<ApiResponse<License>>(`${this.base}/status`).pipe(map(r => r.data));
  }

  activate(licenseKey: string): Observable<License> {
    return this.http.post<ApiResponse<License>>(`${this.base}/activate`, { licenseKey }).pipe(map(r => r.data));
  }
}
