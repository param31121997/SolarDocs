import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';
import {
  CreateProductCategoryRequest,
  ProductCategory,
  UpdateProductCategoryRequest
} from '../models/product-category.model';

@Injectable({ providedIn: 'root' })
export class ProductCategoryService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/master-data/product-categories`;

  list(q?: string, includeInactive = false): Observable<ProductCategory[]> {
    let params = new HttpParams().set('includeInactive', String(includeInactive));
    if (q) params = params.set('q', q);
    return this.http.get<ApiResponse<ProductCategory[]>>(this.base, { params }).pipe(map(r => r.data));
  }

  get(id: string): Observable<ProductCategory> {
    return this.http.get<ApiResponse<ProductCategory>>(`${this.base}/${id}`).pipe(map(r => r.data));
  }

  create(payload: CreateProductCategoryRequest): Observable<ProductCategory> {
    return this.http.post<ApiResponse<ProductCategory>>(this.base, payload).pipe(map(r => r.data));
  }

  update(id: string, payload: UpdateProductCategoryRequest): Observable<ProductCategory> {
    return this.http.put<ApiResponse<ProductCategory>>(`${this.base}/${id}`, payload).pipe(map(r => r.data));
  }

  setActive(id: string, active: boolean): Observable<ProductCategory> {
    return this.http.patch<ApiResponse<ProductCategory>>(`${this.base}/${id}/active`, { active }).pipe(map(r => r.data));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`).pipe(map(() => undefined));
  }
}
