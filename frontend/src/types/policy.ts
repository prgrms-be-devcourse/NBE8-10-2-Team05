// Policy 관련 타입 정의
// 백엔드 API 계약 기반
// GET /api/v1/welfare/policy/search

// ===== 검색 요청 파라미터 =====
// PolicyElasticSearchRequestDto 기반
export interface PolicySearchRequest {
  zipCd?: string | null;
  schoolCode?: string | null;
  jobCode?: string | null;
  keyword?: string | null;
  age?: number | null;
  earn?: number | null;
  regionCode?: string | null;
  marriageStatus?: string | null;
  keywords?: string[] | null;
  from: number;
  size: number;
}

// ===== 검색 드롭다운용 코드 맵 =====
// 백엔드 CodeEnum 기반 (code → 한글 라벨)

// EducationRequirement (학력요건)
export const SchoolCodeLabel: Record<string, string> = {
  "049001": "고졸 미만",
  "049002": "고교 재학",
  "049003": "고졸 예정",
  "049004": "고교 졸업",
  "049005": "대학 재학",
  "049006": "대졸 예정",
  "049007": "대학 졸업",
  "049008": "석·박사",
  "049009": "기타",
  "049010": "제한없음",
};

// JobRequirement (취업요건)
export const JobCodeLabel: Record<string, string> = {
  "013001": "재직자",
  "013002": "자영업자",
  "013003": "미취업자",
  "013004": "프리랜서",
  "013005": "일용근로자",
  "013006": "(예비)창업자",
  "013007": "단기근로자",
  "013008": "영농종사자",
  "013009": "기타",
  "013010": "제한없음",
};

// MarriageRequirement (결혼상태)
export const MarriageStatusCodeLabel: Record<string, string> = {
  "055001": "기혼",
  "055002": "미혼",
  "055003": "제한없음",
};

// ===== 검색 응답 =====
// PolicyDocument 기반
export interface PolicyDocument {
  policyId: number | null;
  plcyNo: string | null;
  plcyNm: string | null;

  // 나이
  minAge: number | null;
  maxAge: number | null;
  ageLimited: boolean | null;

  // 소득
  earnCondition: string | null;
  earnMin: number | null;
  earnMax: number | null;

  // 대상 조건
  regionCode: string | null;
  jobCode: string | null;
  schoolCode: string | null;
  marriageStatus: string | null;

  // 태그 / 분류
  keywords: string[] | null;
  specialBizCode: string | null;

  // 검색용 텍스트
  description: string | null;
}

// ===== 북마크 응답 =====
// GET /api/v1/member/bookmark/welfare-bookmarks
// BookmarkPolicyResponseDto 기반
export interface BookmarkPolicyResponse {
  code: number;
  message: string;
  policies: Policy[] | null;
}

// ===== 북마크 토글 응답 =====
// POST /api/v1/member/bookmark/welfare-bookmarks/{policyId}
// BookmarkUpdateResponseDto 기반
export interface BookmarkUpdateResponse {
  code: number;
  message: string;
}

// ===== 신청 관련 =====
// GET /api/v1/member/policy-aply/welfare-applications
// Application 엔티티 그대로 직렬화 (Policy, Member 포함)
export interface ApplicationItem {
  id: number;
  policy: Policy;
  applicant: ApplicationMember;
  createdAt: string; // ISO 8601
  modifiedAt: string; // ISO 8601
}

// Application 내 Member (엔티티 전체 직렬화)
export interface ApplicationMember {
  id: number;
  name: string;
  email: string;
}

// POST /api/v1/member/policy-aply/welfare-application/{policyId}
// AddApplicationResponseDto 기반
export interface AddApplicationResponse {
  status: number;
  message: string;
}

// PUT /api/v1/member/policy-aply/welfare-application/{id}
// DeleteApplicationResponseDto 기반
export interface DeleteApplicationResponse {
  code: number;
  message: string;
}

// Policy 엔티티 기반
export interface Policy {
  id: number | null;
  plcyNo: string | null;
  plcyNm: string | null;
  plcyKywdNm: string | null;
  plcyExplnCn: string | null;
  plcySprtCn: string | null;
  sprvsnInstCdNm: string | null;
  operInstCdNm: string | null;
  aplyPrdSeCd: string | null;
  bizPrdBgngYmd: string | null;
  bizPrdEndYmd: string | null;
  plcyAplyMthdCn: string | null;
  aplyUrlAddr: string | null;
  sbmsnDcmntCn: string | null;
  sprtTrgtMinAge: string | null;
  sprtTrgtMaxAge: string | null;
  sprtTrgtAgeLmtYn: string | null;
  mrgSttsCd: string | null;
  earnCndSeCd: string | null;
  earnMinAmt: string | null;
  earnMaxAmt: string | null;
  zipCd: string | null;
  jobCd: string | null;
  schoolCd: string | null;
  aplyYmd: string | null;
  sbizCd: string | null;
  rawJson: string | null;
}
