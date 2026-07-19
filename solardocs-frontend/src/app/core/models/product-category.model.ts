export interface ProductCategory {
  id: string;
  categoryCode: string;
  categoryName: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductCategoryRequest {
  categoryName: string;
  description?: string;
}

export interface UpdateProductCategoryRequest {
  categoryName: string;
  description?: string;
}
