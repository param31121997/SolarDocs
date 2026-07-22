import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Customer, CustomerSummary } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/customers`;

  list(q?: string, status?: string): Observable<CustomerSummary[]> {
    let params = new HttpParams();
    if (q) params = params.set('q', q);
    if (status) params = params.set('status', status);
    return this.http.get<ApiResponse<CustomerSummary[]>>(this.base, { params })
      .pipe(map(r => r.data));
  }

  get(id: string): Observable<Customer> {
    return this.http.get<ApiResponse<Customer>>(`${this.base}/${id}`).pipe(map(r => r.data));
  }

  create(payload: Partial<Customer>): Observable<Customer> {
    return this.http.post<ApiResponse<Customer>>(this.base, payload).pipe(map(r => r.data));
  }

  update(id: string, payload: Partial<Customer>): Observable<Customer> {
    return this.http.put<ApiResponse<Customer>>(`${this.base}/${id}`, payload).pipe(map(r => r.data));
  }

  updateStatus(id: string, status: string): Observable<Customer> {
    return this.http.patch<ApiResponse<Customer>>(`${this.base}/${id}/status`, { status }).pipe(map(r => r.data));
  }

  /**
   * Saves the installation/technical facts (module & inverter specs,
   * serial numbers, Aadhaar, inspection info, net meter serial no,
   * agreement place) once on the customer, so the compliance document
   * strategies can pull them instead of asking again per document.
   */
  updatePlantDetails(id: string, payload: Record<string, string | undefined>): Observable<Customer> {
    return this.http.put<ApiResponse<Customer>>(`${this.base}/${id}/plant-details`, payload).pipe(map(r => r.data));
  }

  archive(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`).pipe(map(() => undefined));
  }
}
