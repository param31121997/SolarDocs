import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  upload(customerId: string, docType: string, file: File): Observable<any> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ApiResponse<any>>(
      `${this.base}/customers/${customerId}/documents?docType=${docType}`, form
    ).pipe(map(r => r.data));
  }

  listUploaded(customerId: string): Observable<UploadedDocument[]> {
    return this.http.get<ApiResponse<UploadedDocument[]>>(
      `${this.base}/customers/${customerId}/documents`
    ).pipe(map(r => r.data));
  }

  listTemplates(): Observable<any[]> {
    return this.http.get<ApiResponse<any[]>>(`${this.base}/templates`).pipe(map(r => r.data));
  }

  generate(customerId: string, templateCode: string, extraFields: Record<string, any>): Observable<any> {
    return this.http.post<ApiResponse<any>>(
      `${this.base}/customers/${customerId}/documents/generate`,
      { templateCode, extraFields }
    ).pipe(map(r => r.data));
  }
}

export interface UploadedDocument {
  id: string;
  type: string;
  fileName: string;
  uploadedAt: string;
}
