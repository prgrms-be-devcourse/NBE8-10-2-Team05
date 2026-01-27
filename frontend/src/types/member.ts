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

// ===== 주소 업데이트 =====
// PUT /api/v1/member/member/detail/address

// 카카오 우편번호 API에서 가져온 데이터 (프론트 → 백엔드)
export interface AddressDto {
  postcode: string | null; // 우편번호
  addressName: string | null; // 전체 주소
  sigunguCode: string | null; // 시/군/구 코드
  bCode: string | null; // 법정동/법정리 코드
  roadAddress: string | null; // 도로명 주소
  hCode: string | null; // 행정동 코드 (백엔드에서 채움)
  latitude: number | null; // 위도 (백엔드에서 채움)
  longitude: number | null; // 경도 (백엔드에서 채움)
}

// 회원 상세 정보 응답 (주소 업데이트 후 반환)
export interface MemberDetailRes {
  createdAt: string;
  modifiedAt: string;
  name: string;
  email: string;
  rrnFront: number;
  rrnBackFirst: number;
  type: LoginType;
  role: Role;
  regionCode: string | null;
  marriageStatus: string | null; // enum 값 (추후 정의)
  income: number | null;
  employmentStatus: string | null; // enum 값 (추후 정의)
  educationLevel: string | null; // enum 값 (추후 정의)
  postcode: string | null;
  roadAddress: string | null;
  hCode: string | null;
  latitude: number | null;
  longitude: number | null;
}

// ===== 에러 응답 =====
export interface ApiErrorResponse {
  resultCode: string;
  msg: string;
}
