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
    <header className="ds-header">
      <Link href="/" className="ds-header-logo">
        통합 복지 서비스
      </Link>
      <nav className="ds-header-nav">
        <Link href="/">정책 검색</Link>
        <Link href="/estate">행복주택</Link>
        <Link href="/welfare/center">시설찾기</Link>
        <Link href="/welfare/lawyer">노무사 찾기</Link>
        <Link href="/bookmark">북마크</Link>
        <Link href="/applications">신청내역</Link>
      </nav>
      <div className="ds-header-user">
        {isLoading ? (
          <span className="ds-meta">로딩중...</span>
        ) : user ? (
          <>
            <Link href="/mypage">{user.name}님</Link>
            <button type="button" onClick={handleLogout} className="ds-header-logout">
              로그아웃
            </button>
          </>
        ) : (
          <>
            <Link href="/login">로그인</Link>
            <Link href="/join">회원가입</Link>
          </>
        )}
      </div>
    </header>
  );
}
