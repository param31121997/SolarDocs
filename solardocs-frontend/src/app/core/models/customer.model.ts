export interface CustomerSummary {
  customerId: string;
  name: string;
  mobile: string;
  status: string;
  village: string;
  applicationNumber: string;
}

export interface Customer {
  customerId: string;
  name: string;
  mobile: string;
  alternateMobile?: string;
  addressLine?: string;
  village?: string;
  district?: string;
  state?: string;
  pincode?: string;
  consumerNumber?: string;
  applicationNumber?: string;
  sanctionedLoadKw?: number;
  plantCapacityKw?: number;
  discom?: string;
  category?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: { code: string; message: string } | null;
  timestamp: string;
}
