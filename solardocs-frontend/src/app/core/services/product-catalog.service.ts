import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

export interface Product {
  code: string;
  name: string;
  type: string;
  unit: string;
  defaultRate: number;
  defaultGstPercent: string;
}

@Injectable({ providedIn: 'root' })
export class ProductCatalogService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/settings/product-catalog`;

  list(): Observable<Product[]> {
    return this.http.get<ApiResponse<Product[]>>(this.base).pipe(map(r => r.data));
  }

  save(products: Product[]): Observable<Product[]> {
    return this.http.put<ApiResponse<Product[]>>(this.base, products).pipe(map(r => r.data));
  }
}
