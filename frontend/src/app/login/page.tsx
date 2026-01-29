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

  // 소셜 로그인 핸들러
  const handleSocialLogin = (provider: "kakao" | "naver") => {
    // 백엔드 OAuth2 엔드포인트로 리다이렉트
    window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
  };

  return (
    <div>
      <main style={{ padding: "20px", maxWidth: "400px", margin: "50px auto" }}>
        <h1>로그인</h1>

        {error && (
          <div
            style={{
              color: "red",
              padding: "10px",
              marginBottom: "20px",
              border: "1px solid red",
              borderRadius: "4px",
            } as React.CSSProperties}
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ marginBottom: "30px" }}>
          <div style={{ marginBottom: "15px" }}>
            <label style={{ display: "block", marginBottom: "5px" }}>
              이메일
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{
                width: "100%",
                padding: "10px",
                border: "1px solid #ddd",
                borderRadius: "4px",
              } as React.CSSProperties}
            />
          </div>

          <div style={{ marginBottom: "20px" }}>
            <label style={{ display: "block", marginBottom: "5px" }}>
              비밀번호
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{
                width: "100%",
                padding: "10px",
                border: "1px solid #ddd",
                borderRadius: "4px",
              } as React.CSSProperties}
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            style={{
              width: "100%",
              padding: "12px",
              backgroundColor: isLoading ? "#ccc" : "#007bff",
              color: "white",
              border: "none",
              borderRadius: "4px",
              cursor: isLoading ? "not-allowed" : "pointer",
              fontSize: "16px",
            } as React.CSSProperties}
          >
            {isLoading ? "로그인 중..." : "로그인"}
          </button>
        </form>

        {/* 구분선 */}
        <div
          style={{
            textAlign: "center",
            margin: "20px 0",
            position: "relative",
          }}
        >
          <hr style={{ border: "none", borderTop: "1px solid #ddd" }} />
          <span
            style={{
              position: "absolute",
              top: "-12px",
              left: "50%",
              transform: "translateX(-50%)",
              backgroundColor: "white",
              padding: "0 10px",
              color: "#666",
            }}
          >
            또는
          </span>
        </div>

        {/* 소셜 로그인 버튼 */}
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <button
            onClick={() => handleSocialLogin("kakao")}
            style={{
              width: "100%",
              padding: "0",
              backgroundColor: "transparent",
              border: "none",
              cursor: "pointer",
              display: "flex",
              justifyContent: "center",
            }}
          >
            <img
              src="/images/kakao_login_medium_narrow.png"
              alt="카카오 로그인"
              style={{
                width: "183px",
                height: "43px",
              }}
            />
          </button>
        </div>

        {/* 회원가입 링크 */}
        <div
          style={{
            marginTop: "20px",
            textAlign: "center",
            color: "#666",
          }}
        >
          계정이 없으신가요?{" "}
          <a href="/join" style={{ color: "#007bff", textDecoration: "none" }}>
            회원가입
          </a>
        </div>
      </main>
    </div>
  );
}
