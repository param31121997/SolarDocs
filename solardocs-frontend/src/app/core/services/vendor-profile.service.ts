import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/customer.model';

export interface VendorProfile {
  companyName: string;
  gstin: string;
  registeredAddress: string;
  logoPath: string;
  bankAccountName: string;
  bankAccountNumber: string;
  bankIfsc: string;
  signatoryName: string;
}

@Injectable({ providedIn: 'root' })
export class VendorProfileService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/settings/vendor-profile`;

  get(): Observable<VendorProfile> {
    return this.http.get<ApiResponse<VendorProfile>>(this.base).pipe(map(r => r.data));
  }

  update(profile: VendorProfile): Observable<VendorProfile> {
    return this.http.put<ApiResponse<VendorProfile>>(this.base, profile).pipe(map(r => r.data));
  }
}
