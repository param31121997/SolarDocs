import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';
import { CreateItemRequest, Item, UpdateItemRequest } from '../models/item.model';

@Injectable({ providedIn: 'root' })
export class ItemService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/master/items`;

  list(q?: string, includeInactive = false): Observable<Item[]> {
    let params = new HttpParams().set('includeInactive', String(includeInactive));
    if (q) params = params.set('q', q);
    return this.http.get<ApiResponse<Item[]>>(this.base, { params }).pipe(map(r => r.data));
  }

  get(id: string): Observable<Item> {
    return this.http.get<ApiResponse<Item>>(`${this.base}/${id}`).pipe(map(r => r.data));
  }

  create(payload: CreateItemRequest): Observable<Item> {
    return this.http.post<ApiResponse<Item>>(this.base, payload).pipe(map(r => r.data));
  }

  update(id: string, payload: UpdateItemRequest): Observable<Item> {
    return this.http.put<ApiResponse<Item>>(`${this.base}/${id}`, payload).pipe(map(r => r.data));
  }

  setActive(id: string, active: boolean): Observable<Item> {
    return this.http.patch<ApiResponse<Item>>(`${this.base}/${id}/active`, { active }).pipe(map(r => r.data));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`).pipe(map(() => undefined));
  }
}
