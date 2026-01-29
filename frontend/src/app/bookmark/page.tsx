"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Header from "@/components/Header";
import type { Policy } from "@/types/policy";
import { getBookmarks } from "@/api/bookmark";
import { useAuth } from "@/contexts/AuthContext";

export default function BookmarkPage() {
  const { user, isLoading: authLoading } = useAuth();

  const [policies, setPolicies] = useState<Policy[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      setLoading(false);
      return;
    }

    const fetchBookmarks = async () => {
      try {
        const data = await getBookmarks();
        if (data.code === 200) {
          setPolicies(data.policies ?? []);
        } else {
          setError(data.message || "북마크를 불러오지 못했습니다.");
        }
      } catch (err: unknown) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("북마크를 불러오는 중 오류가 발생했습니다.");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchBookmarks();
  }, [user, authLoading]);

  if (authLoading || loading) {
    return (
      <div>
        <Header />
        <main style={{ padding: "20px" }}>
          <h1>북마크</h1>
          <div>로딩 중...</div>
        </main>
      </div>
    );
  }

  if (!user) {
    return (
      <div>
        <Header />
        <main style={{ padding: "20px" }}>
          <h1>북마크</h1>
          <p>북마크를 이용하려면 로그인이 필요합니다.</p>
          <Link href="/login">로그인 페이지로 이동</Link>
        </main>
      </div>
    );
  }

  return (
    <div>
      <Header />
      <main style={{ padding: "20px" }}>
        <h1>북마크</h1>

        {error && (
          <div style={{ color: "red", marginTop: "12px" }}>
            오류: {error}
          </div>
        )}

        <div style={{ marginTop: "12px" }}>
          <div>북마크된 정책: {policies.length}건</div>

          {policies.length === 0 ? (
            <div style={{ marginTop: "8px" }}>북마크된 정책이 없습니다.</div>
          ) : (
            <div>
              {policies.map((policy, index) => (
                <div
                  key={policy.id ?? index}
                  style={{
                    border: "1px solid #ccc",
                    padding: "12px",
                    marginTop: "8px",
                  }}
                >
                  <div>
                    <strong>{policy.plcyNm ?? "(정책명 없음)"}</strong>
                  </div>
                  <div>정책번호: {policy.plcyNo ?? "-"}</div>
                  {policy.plcyExplnCn && (
                    <div style={{ marginTop: "4px" }}>{policy.plcyExplnCn}</div>
                  )}
                  <div style={{ marginTop: "4px", fontSize: "14px", color: "#666" }}>
                    {policy.sprtTrgtMinAge != null && policy.sprtTrgtMaxAge != null && (
                      <span>연령: {policy.sprtTrgtMinAge}~{policy.sprtTrgtMaxAge}세 | </span>
                    )}
                    {policy.zipCd && <span>지역: {policy.zipCd} | </span>}
                    {policy.mrgSttsCd && <span>결혼: {policy.mrgSttsCd} | </span>}
                    {policy.plcyKywdNm && (
                      <span>태그: {policy.plcyKywdNm}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
