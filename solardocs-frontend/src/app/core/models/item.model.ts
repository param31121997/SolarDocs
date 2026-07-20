export interface Item {
  id: string;
  itemName: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateItemRequest {
  itemName: string;
  description?: string;
}

export interface UpdateItemRequest {
  itemName: string;
  description?: string;
}
