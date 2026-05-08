export interface Product {
  id: number;
  brandId: number;
  categoryId: number;
  name: string;
  slug: string;
  description: string;
  basePrice: number | string;
  thumbnail: string;
  isActive: boolean;
  isFeatured: boolean;
  viewCount: number;
}

export interface ProductRequest {
  brandId: number;
  categoryId: number;
  name: string;
  slug: string;
  description: string;
  basePrice: number;
  thumbnail: string;
  isActive: boolean;
  isFeatured: boolean;
}
