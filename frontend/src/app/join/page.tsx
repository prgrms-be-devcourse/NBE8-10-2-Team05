"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { join, ApiError } from "@/api/member";
import type { JoinRequest } from "@/types/member";

export default function JoinPage() {
  const router = useRouter();

  // 폼 상태
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rrnFront, setRrnFront] = useState("");
  const [rrnBackFirst, setRrnBackFirst] = useState("");

  // UI 상태
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const request: JoinRequest = {
      name,
      email,
      password,
      rrnFront: Number(rrnFront),
      rrnBackFirst: Number(rrnBackFirst),
    };

    try {
      await join(request);
      // 회원가입 성공 시 로그인 페이지로 이동
      router.push("/login");
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
    <main className="ds-page-narrow">
      <h1 className="ds-title">회원가입</h1>

      {error && <div className="ds-alert-error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="ds-form-group">
          <label className="ds-label">이름</label>
          <input
            type="text"
            className="ds-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>

        <div className="ds-form-group">
          <label className="ds-label">이메일</label>
          <input
            type="email"
            className="ds-input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className="ds-form-group">
          <label className="ds-label">비밀번호</label>
          <input
            type="password"
            className="ds-input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <div className="ds-form-group">
          <label className="ds-label">주민등록번호 앞자리 (6자리)</label>
          <input
            type="text"
            className="ds-input"
            value={rrnFront}
            onChange={(e) => setRrnFront(e.target.value)}
            maxLength={6}
            pattern="[0-9]{6}"
            required
          />
        </div>

        <div className="ds-form-group">
          <label className="ds-label">주민등록번호 뒷자리 첫번째 (1자리)</label>
          <input
            type="text"
            className="ds-input"
            value={rrnBackFirst}
            onChange={(e) => setRrnBackFirst(e.target.value)}
            maxLength={1}
            pattern="[0-9]{1}"
            required
          />
        </div>

        <button type="submit" disabled={isLoading} className="ds-btn ds-btn-primary ds-btn-full">
          {isLoading ? "가입 중..." : "회원가입"}
        </button>
      </form>

      <div style={{ marginTop: "24px", textAlign: "center" }}>
        <span className="ds-meta">이미 계정이 있으신가요? </span>
        <a href="/login" className="ds-link">로그인</a>
      </div>
    </main>
  );
}
