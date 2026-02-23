"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { completeSocialSignup } from "@/api/member";

export default function SocialSignupPage() {
  const router = useRouter();

  const [rrnFront, setRrnFront] = useState("");
  const [rrnBackFirst, setRrnBackFirst] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    // 유효성 검사
    if (rrnFront.length !== 6) {
      setError("주민등록번호 앞자리는 6자리여야 합니다.");
      setIsLoading(false);
      return;
    }

    if (rrnBackFirst.length !== 1) {
      setError("주민등록번호 뒷자리 첫번째는 1자리여야 합니다.");
      setIsLoading(false);
      return;
    }

    try {
      await completeSocialSignup({ rrnFront, rrnBackFirst });
      // 성공 시 메인 페이지로 이동
      router.push("/");
    } catch (err: any) {
      setError(err.message || "추가 정보 입력에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ padding: "20px", maxWidth: "500px", margin: "50px auto" }}>
      <h1 style={{ marginBottom: "10px" }}>추가 정보 입력</h1>
      <p style={{ color: "#666", marginBottom: "30px" }}>
        복지 혜택 조회를 위해 주민등록번호가 필요합니다.
      </p>

      {error && (
        <div
          style={{
            color: "red",
            padding: "10px",
            marginBottom: "20px",
            border: "1px solid red",
            borderRadius: "4px",
            backgroundColor: "#fff5f5",
          } as React.CSSProperties}
        >
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: "20px" }}>
          <label
            style={{
              display: "block",
              marginBottom: "8px",
              fontWeight: "bold",
            }}
          >
            주민등록번호
          </label>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <input
              type="text"
              value={rrnFront}
              onChange={(e) => {
                const value = e.target.value.replace(/\D/g, ""); // 숫자만
                if (value.length <= 6) setRrnFront(value);
              }}
              placeholder="앞 6자리"
              maxLength={6}
              required
              style={{
                flex: 1,
                padding: "12px",
                border: "1px solid #ddd",
                borderRadius: "4px",
                fontSize: "16px",
              } as React.CSSProperties}
            />
            <span style={{ fontSize: "20px", fontWeight: "bold" }}>-</span>
            <input
              type="text"
              value={rrnBackFirst}
              onChange={(e) => {
                const value = e.target.value.replace(/\D/g, ""); // 숫자만
                if (value.length <= 1) setRrnBackFirst(value);
              }}
              placeholder="뒷자리 첫번째"
              maxLength={1}
              required
              style={{
                width: "60px",
                padding: "12px",
                border: "1px solid #ddd",
                borderRadius: "4px",
                fontSize: "16px",
                textAlign: "center",
              } as React.CSSProperties}
            />
            <span style={{ color: "#999" }}>● ● ● ● ● ●</span>
          </div>
          <small style={{ color: "#666", display: "block", marginTop: "5px" }}>
            예: 990101-1
          </small>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          style={{
            width: "100%",
            padding: "14px",
            backgroundColor: isLoading ? "#ccc" : "#007bff",
            color: "white",
            border: "none",
            borderRadius: "4px",
            cursor: isLoading ? "not-allowed" : "pointer",
            fontSize: "16px",
            fontWeight: "bold",
          } as React.CSSProperties}
        >
          {isLoading ? "처리 중..." : "가입 완료"}
        </button>
      </form>
    </div>
  );
}
