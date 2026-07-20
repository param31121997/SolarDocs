export interface Item {
  id: string;
  itemName: string;
  description?: string;
  type?: string;
  unit?: string;
  defaultRate?: number;
  defaultGstPercent?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateItemRequest {
  itemName: string;
  description?: string;
  type?: string;
  unit?: string;
  defaultRate?: number | null;
  defaultGstPercent?: string;
}

export interface UpdateItemRequest {
  itemName: string;
  description?: string;
  type?: string;
  unit?: string;
  defaultRate?: number | null;
  defaultGstPercent?: string;
}
