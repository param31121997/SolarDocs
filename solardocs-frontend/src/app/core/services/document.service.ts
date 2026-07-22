import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  /**
   * Backend business failures (e.g. PDF generation errors) come back as
   * HTTP 200 with { success: false, data: null, error: {...} } - not as
   * an HTTP error status. Unwrapping data() without checking success left
   * callers with a silent `null` on failure (see the "Cannot read
   * properties of null" crash this replaced). Throwing here routes
   * backend failures into the Observable's error channel instead, so
   * subscribe({ error }) actually gets a chance to handle them.
   */
  private unwrap<T>(r: ApiResponse<T>): T {
    if (!r.success) {
      throw new Error(r.error?.message ?? 'Request failed');
    }
    return r.data;
  }

  upload(customerId: string, docType: string, file: File): Observable<any> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ApiResponse<any>>(
      `${this.base}/customers/${customerId}/documents?docType=${docType}`, form
    ).pipe(map(r => this.unwrap(r)));
  }

  listUploaded(customerId: string): Observable<UploadedDocument[]> {
    return this.http.get<ApiResponse<UploadedDocument[]>>(
      `${this.base}/customers/${customerId}/documents`
    ).pipe(map(r => this.unwrap(r)));
  }

  listTemplates(): Observable<any[]> {
    return this.http.get<ApiResponse<any[]>>(`${this.base}/templates`).pipe(map(r => this.unwrap(r)));
  }

  generate(customerId: string, templateCode: string, extraFields: Record<string, any>): Observable<any> {
    return this.http.post<ApiResponse<any>>(
      `${this.base}/customers/${customerId}/documents/generate`,
      { templateCode, extraFields }
    ).pipe(map(r => this.unwrap(r)));
  }

  /**
   * One-click generation of the full compliance document set (Work
   * Completion Report, Guarantee Certificate, Annexure-I, Proforma-A,
   * DCR Declaration, Net Meter Agreement), merged into a single PDF.
   * No form fields - everything comes from the Customer record
   * (including Plant Details) and the vendor's Settings profile.
   */
  generatePackage(customerId: string): Observable<any> {
    return this.http.post<ApiResponse<any>>(
      `${this.base}/customers/${customerId}/documents/generate-package`, {}
    ).pipe(map(r => this.unwrap(r)));
  }
}

export interface UploadedDocument {
  id: string;
  type: string;
  fileName: string;
  uploadedAt: string;
}
