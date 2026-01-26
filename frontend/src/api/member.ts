// Member API 호출 함수
import type {
  JoinRequest,
  JoinResponse,
  LoginRequest,
  LoginResponse,
  ApiErrorResponse,
} from "@/types/member";

const API_BASE_URL = "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    public resultCode: string,
    public msg: string
  ) {
    super(`${resultCode}: ${msg}`);
    this.name = "ApiError";
  }
}

export async function join(request: JoinRequest): Promise<JoinResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/member/member/join`, {
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

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/member/member/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include", // 쿠키 자동 포함 (Set-Cookie 수신, Cookie 전송)
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: LoginResponse = await response.json();
  return data;
}
