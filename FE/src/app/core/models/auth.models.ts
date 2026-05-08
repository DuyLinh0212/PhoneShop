import { AppUser } from './user.models';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
  fullName: string;
  email: string;
  role: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phone?: string;
}

export interface RegisterResponse {
  userId: number;
  fullName: string;
  email: string;
  role: string;
}

export interface AuthSession {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AppUser;
}
