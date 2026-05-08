export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string;
  isActive: boolean;
}

export interface CategoryRequest {
  name: string;
  slug: string;
  description: string;
  isActive: boolean;
}
