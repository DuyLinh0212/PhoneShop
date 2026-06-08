export interface Product {
  id: number;
  brandId: number;
  categoryId: number;
  name: string;
  slug: string;
  description: string;
  basePrice: number | string;
  originalPrice?: number | string | null;
  salePrice?: number | string | null;
  discountPercent?: number | string | null;
  thumbnail: string;
  isActive: boolean;
  isFeatured: boolean;
  viewCount: number;
  totalStock?: number;
  averageRating?: number;
  reviewCount?: number;
}
