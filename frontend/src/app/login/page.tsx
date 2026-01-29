"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { login, ApiError } from "@/api/member";
import { useAuth } from "@/contexts/AuthContext";
import type { LoginRequest } from "@/types/member";

export default function LoginPage() {
  const router = useRouter();
  const { loginUser } = useAuth();

  // 폼 상태
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // UI 상태
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const request: LoginRequest = {
      email,
      password,
    };

    try {
      const response = await login(request);
      // Context + localStorage에 저장
      loginUser({
        memberId: response.memberId,
        name: response.name,
      });
      // 메인 페이지로 이동
      router.push("/");
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

  return (
    <div>
      <main style={{ padding: "20px" }}>
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
      </main>
    </div>
  );
}
