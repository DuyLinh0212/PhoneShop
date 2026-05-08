export interface Brand {
  id: number;
  name: string;
  slug: string;
  logo: string;
  isActive: boolean;
}

export interface BrandRequest {
  name: string;
  slug: string;
  logo: string;
  isActive: boolean;
}
