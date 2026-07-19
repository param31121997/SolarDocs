import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  summary(): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${environment.apiBaseUrl}/dashboard/summary`).pipe(map(r => r.data));
  }
}
