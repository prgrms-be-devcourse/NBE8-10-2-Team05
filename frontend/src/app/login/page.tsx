"use client";

import { useState } from "react";
import { login, ApiError } from "@/api/member";
import type { LoginRequest, LoginResponse } from "@/types/member";

export default function LoginPage() {
  // 폼 상태
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // UI 상태
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successData, setSuccessData] = useState<LoginResponse | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setSuccessData(null);
    setIsLoading(true);

    const request: LoginRequest = {
      email,
      password,
    };

    try {
      const response = await login(request);
      setSuccessData(response);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(`[${err.resultCode}] ${err.msg}`);
      } else {
        setError("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  // 로그인 성공 화면
  if (successData) {
    return (
      <div>
        <h1>로그인 성공</h1>
        <div>
          <p>회원 ID: {successData.memberId}</p>
          <p>이름: {successData.name}</p>
          <p>accessToken 쿠키가 설정되었습니다.</p>
        </div>
      </div>
    );
  }

  // 로그인 폼
  return (
    <div>
      <h1>로그인</h1>

      {error && <div style={{ color: "red" }}>{error}</div>}

      <form onSubmit={handleSubmit}>
        <div>
          <label>이메일</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div>
          <label>비밀번호</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <button type="submit" disabled={isLoading}>
          {isLoading ? "로그인 중..." : "로그인"}
        </button>
      </form>
    </div>
  );
}
