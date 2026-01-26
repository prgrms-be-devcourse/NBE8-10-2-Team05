"use client";

import { useState } from "react";
import { join, ApiError } from "@/api/member";
import type { JoinRequest, JoinResponse } from "@/types/member";

export default function JoinPage() {
  // 폼 상태
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rrnFront, setRrnFront] = useState("");
  const [rrnBackFirst, setRrnBackFirst] = useState("");

  // UI 상태
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successData, setSuccessData] = useState<JoinResponse | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setSuccessData(null);
    setIsLoading(true);

    const request: JoinRequest = {
      name,
      email,
      password,
      rrnFront: Number(rrnFront),
      rrnBackFirst: Number(rrnBackFirst),
    };

    try {
      const response = await join(request);
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

  // 성공 화면
  if (successData) {
    return (
      <div>
        <h1>회원가입 성공</h1>
        <div>
          <p>ID: {successData.id}</p>
          <p>이름: {successData.name}</p>
          <p>이메일: {successData.email}</p>
          <p>가입 유형: {successData.type}</p>
          <p>권한: {successData.role}</p>
          <p>가입일시: {successData.createdAt}</p>
        </div>
      </div>
    );
  }

  // 회원가입 폼
  return (
    <div>
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
    </div>
  );
}
