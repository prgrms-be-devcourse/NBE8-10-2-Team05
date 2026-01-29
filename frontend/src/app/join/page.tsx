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
    <div>
      <main style={{ padding: "20px" }}>
        <h1>회원가입</h1>

        {error && <div style={{ color: "red" }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div>
            <label>이름</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

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

          <div>
            <label>주민등록번호 앞자리 (6자리)</label>
            <input
              type="text"
              value={rrnFront}
              onChange={(e) => setRrnFront(e.target.value)}
              maxLength={6}
              pattern="[0-9]{6}"
              required
            />
          </div>

          <div>
            <label>주민등록번호 뒷자리 첫번째 (1자리)</label>
            <input
              type="text"
              value={rrnBackFirst}
              onChange={(e) => setRrnBackFirst(e.target.value)}
              maxLength={1}
              pattern="[0-9]{1}"
              required
            />
          </div>

          <button type="submit" disabled={isLoading}>
            {isLoading ? "가입 중..." : "회원가입"}
          </button>
        </form>
      </main>
    </div>
  );
}
