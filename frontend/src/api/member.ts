// Member API 호출 함수
import type {
  JoinRequest,
  JoinResponse,
  LoginRequest,
  LoginResponse,
  AddressDto,
  MemberDetailRes,
  ApiErrorResponse,
} from "@/types/member";

export class ApiError extends Error {
  constructor(
    public resultCode: string,
    public msg: string
  ) {
    super(`${resultCode}: ${msg}`);
    this.name = "ApiError";
  }
}

// accessToken 재발급 요청
// POST /api/v1/auth/reissue
async function reissue(): Promise<boolean> {
  const response = await fetch(`/api/v1/auth/reissue`, {
    method: "POST",
    credentials: "include",
  });
  return response.ok;
}

// 인증이 필요한 API 호출 래퍼
// 401 발생 시 → reissue → 성공하면 원래 요청 재시도
async function fetchWithAuth(
  url: string,
  options: RequestInit
): Promise<Response> {
  const response = await fetch(url, {
    ...options,
    credentials: "include",
  });

  if (response.status === 401) {
    console.log("[fetchWithAuth] 401 감지, reissue 시도...");
    try {
      const reissued = await reissue();
      console.log("[fetchWithAuth] reissue 결과:", reissued);
      if (reissued) {
        console.log("[fetchWithAuth] 원래 요청 재시도...");
        return fetch(url, {
          ...options,
          credentials: "include",
        });
      }
    } catch (reissueErr) {
      console.error("[fetchWithAuth] reissue 실패:", reissueErr);
    }
  }

  return response;
}

// ===== 회원가입 (인증 불필요) =====
export async function join(request: JoinRequest): Promise<JoinResponse> {
  const response = await fetch(`/api/v1/member/member/join`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: JoinResponse = await response.json();
  return data;
}

// ===== 로그인 (인증 불필요) =====
export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`/api/v1/member/member/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: LoginResponse = await response.json();
  return data;
}

// ===== 주소 업데이트 (인증 필요) =====
export async function updateAddress(
  addressDto: AddressDto
): Promise<MemberDetailRes> {
  const response = await fetchWithAuth(
    `/api/v1/member/member/detail/address`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(addressDto),
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: MemberDetailRes = await response.json();
  return data;
}

// ===== 로그아웃 (인증 필요) =====
export async function logout(): Promise<void> {
  const response = await fetchWithAuth(
    `/api/v1/member/member/logout`,
    {
      method: "POST",
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }
}
