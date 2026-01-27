"use client";

import Link from "next/link";
import { useAuth } from "@/contexts/AuthContext";

export default function Header() {
  const { user, isLoading } = useAuth();

  return (
    <header style={{ display: "flex", gap: "20px", padding: "16px", borderBottom: "1px solid #ccc" }}>
      <Link href="/" style={{ fontWeight: "bold" }}>
        통합 복지 서비스
      </Link>
      <span>|</span>
      <span>정책 검색</span>
      <span>|</span>
      <span>행복주택</span>
      <span>|</span>
      <span>시설찾기</span>
      <span>|</span>
      {isLoading ? (
        <span>로딩중...</span>
      ) : user ? (
        <span>{user.name}님 환영합니다</span>
      ) : (
        <span>
          <Link href="/login">로그인</Link>
          {" / "}
          <Link href="/join">회원가입</Link>
        </span>
      )}
    </header>
  );
}
