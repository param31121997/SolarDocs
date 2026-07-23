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
  email?: string;
  aadhaarNumber?: string;
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
  // Installation/technical facts filled once on the "Plant Details" tab and
  // reused by every compliance document strategy - see PlantInstallationDetails.java
  // (email/Aadhaar live above with the rest of the consumer's identity, not here)
  plantInstallationDate?: string;
  plantInverterMake?: string;
  plantInverterRating?: string;
  plantInverterCapacityKw?: string;
  plantChargeControllerType?: string;
  plantHpd?: string;
  plantEarthing1Ohms?: string;
  plantEarthing2Ohms?: string;
  plantEarthing3Ohms?: string;
  plantModuleWattage?: string;
  plantModuleCount?: string;
  plantModuleCapacityKw?: string;
  plantModuleSerialNumbers?: string;
  plantCellManufacturerName?: string;
  plantCellGstInvoiceNo?: string;
  plantInspectionDate?: string;
  plantInspectionLetterNo?: string;
  plantInspectionLetterDate?: string;
  plantAgreementPlace?: string;
  plantNetMeterSerialNo?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: { code: string; message: string } | null;
  timestamp: string;
}
