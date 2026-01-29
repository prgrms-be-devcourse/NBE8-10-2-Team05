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
