"use client";

import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/contexts/AuthContext";
import { logout, ApiError } from "@/api/member";

export default function Header() {
  const router = useRouter();
  const { user, isLoading, logoutUser } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      // 서버 로그아웃 실패해도 클라이언트 상태는 정리
      if (err instanceof ApiError) {
        console.error(`로그아웃 실패: [${err.resultCode}] ${err.msg}`);
      }
    }
    // Context + localStorage 정리
    logoutUser();
    router.push("/");
  };

  return (
    <header style={{ display: "flex", gap: "20px", padding: "16px", borderBottom: "1px solid #ccc", alignItems: "center" }}>
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
        <span>
          <Link href="/mypage">{user.name}님 환영합니다</Link>
          {" | "}
          <button type="button" onClick={handleLogout}>
            로그아웃
          </button>
        </span>
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
