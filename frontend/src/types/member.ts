// Member 관련 타입 정의
// 백엔드 API 계약 기반

// ===== Enum =====
export const LoginType = {
  EMAIL: "EMAIL",
  NAVER: "NAVER",
} as const;

export type LoginType = (typeof LoginType)[keyof typeof LoginType];

export const Role = {
  ADMIN: "ADMIN",
  USER: "USER",
} as const;

export type Role = (typeof Role)[keyof typeof Role];

// ===== 회원가입 =====
// POST /api/v1/member/member/join

export interface JoinRequest {
  name: string;
  email: string;
  password: string;
  rrnFront: number; // 주민등록번호 앞 6자리
  rrnBackFirst: number; // 주민등록번호 뒷자리 첫 번째 1자리
}

export interface JoinResponse {
  id: number;
  name: string;
  email: string;
  type: LoginType;
  role: Role;
  createdAt: string; // ISO 8601 형식 (LocalDateTime → string)
}

// ===== 로그인 =====
// POST /api/v1/member/member/login

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  memberId: number;
  name: string;
  accessToken: string;
}

// ===== 에러 응답 =====
export interface ApiErrorResponse {
  resultCode: string;
  msg: string;
}
