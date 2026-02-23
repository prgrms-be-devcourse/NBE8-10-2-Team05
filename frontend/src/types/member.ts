// Member 관련 타입 정의
// 백엔드 API 계약 기반

// ===== Enum =====
export const LoginType = {
  EMAIL: "EMAIL",
  NAVER: "NAVER",
  KAKAO: "KAKAO",
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

// 회원 상세 정보 응답
export interface MemberDetailRes {
  createdAt: string;
  modifiedAt: string;
  name: string;
  email: string;
  rrnFront: string;
  rrnBackFirst: string;
  type: LoginType;
  role: Role;
  regionCode: string | null;
  marriageStatus: MarriageStatus | null;
  income: number | null;
  employmentStatus: EmploymentStatus | null;
  educationLevel: EducationLevel | null;
  specialStatus: string | null;
  postcode: string | null;
  roadAddress: string | null;
  hCode: string | null;
  latitude: number | null;
  longitude: number | null;
}

// 회원 상세 정보 수정 요청
// PUT /api/v1/member/member/detail
export interface MemberDetailReq {
  name?: string | null;
  email?: string | null;
  regionCode?: string | null;
  marriageStatus?: MarriageStatus | null;
  income?: number | null;
  employmentStatus?: EmploymentStatus | null;
  educationLevel?: EducationLevel | null;
  specialStatus?: string | null;
}

// ===== Enum: 결혼 상태 =====
export const MarriageStatus = {
  MARRIED: "MARRIED",
  SINGLE: "SINGLE",
} as const;

export type MarriageStatus = (typeof MarriageStatus)[keyof typeof MarriageStatus];

export const MarriageStatusLabel: Record<MarriageStatus, string> = {
  MARRIED: "기혼",
  SINGLE: "미혼",
};

// ===== Enum: 고용 상태 =====
export const EmploymentStatus = {
  EMPLOYED: "EMPLOYED",
  SELF_EMPLOYED: "SELF_EMPLOYED",
  UNEMPLOYED: "UNEMPLOYED",
  FREELANCER: "FREELANCER",
} as const;

export type EmploymentStatus = (typeof EmploymentStatus)[keyof typeof EmploymentStatus];

export const EmploymentStatusLabel: Record<EmploymentStatus, string> = {
  EMPLOYED: "재직자",
  SELF_EMPLOYED: "자영업자",
  UNEMPLOYED: "미취업자",
  FREELANCER: "프리랜서",
};

// ===== Enum: 학력 =====
export const EducationLevel = {
  BELOW_HIGH_SCHOOL: "BELOW_HIGH_SCHOOL",
  HIGH_SCHOOL_ENROLLED: "HIGH_SCHOOL_ENROLLED",
  HIGH_SCHOOL_EXPECTED: "HIGH_SCHOOL_EXPECTED",
  HIGH_SCHOOL_GRADUATED: "HIGH_SCHOOL_GRADUATED",
  UNIVERSITY_ENROLLED: "UNIVERSITY_ENROLLED",
  UNIVERSITY_EXPECTED: "UNIVERSITY_EXPECTED",
  UNIVERSITY_GRADUATED: "UNIVERSITY_GRADUATED",
  MASTER_DOCTOR: "MASTER_DOCTOR",
} as const;

export type EducationLevel = (typeof EducationLevel)[keyof typeof EducationLevel];

export const EducationLevelLabel: Record<EducationLevel, string> = {
  BELOW_HIGH_SCHOOL: "고졸 미만",
  HIGH_SCHOOL_ENROLLED: "고교 재학",
  HIGH_SCHOOL_EXPECTED: "고졸 예정",
  HIGH_SCHOOL_GRADUATED: "고교 졸업",
  UNIVERSITY_ENROLLED: "대학 재학",
  UNIVERSITY_EXPECTED: "대졸 예정",
  UNIVERSITY_GRADUATED: "대학 졸업",
  MASTER_DOCTOR: "석·박사",
};

// ===== Enum: 특수 상태 =====
export const SpecialStatus = {
  SME: "SME",
  WOMEN: "WOMEN",
  BASIC_LIVELIHOOD: "BASIC_LIVELIHOOD",
  SINGLE_PARENT: "SINGLE_PARENT",
  DISABLED: "DISABLED",
  FARMER: "FARMER",
  SOLDIER: "SOLDIER",
  LOCAL_TALENT: "LOCAL_TALENT",
} as const;

export type SpecialStatus = (typeof SpecialStatus)[keyof typeof SpecialStatus];

export const SpecialStatusLabel: Record<SpecialStatus, string> = {
  SME: "중소기업",
  WOMEN: "여성",
  BASIC_LIVELIHOOD: "기초생활수급자",
  SINGLE_PARENT: "한부모가정",
  DISABLED: "장애인",
  FARMER: "농업인",
  SOLDIER: "군인",
  LOCAL_TALENT: "지역인재",
};

// ===== 에러 응답 =====
export interface ApiErrorResponse {
  resultCode: string;
  msg: string;
}
