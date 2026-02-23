"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import type { Policy } from "@/types/policy";
import { getBookmarks, toggleBookmark } from "@/api/bookmark";
import { useAuth } from "@/contexts/AuthContext";

export default function BookmarkPage() {
  const { user, isLoading: authLoading } = useAuth();

  const [policies, setPolicies] = useState<Policy[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [removingIds, setRemovingIds] = useState<Set<number>>(new Set());

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

    void fetchBookmarks();
  }, [user, authLoading]);

  const handleRemoveBookmark = async (policyId: number) => {
    if (!user) {
      setError("북마크를 취소하려면 로그인이 필요합니다.");
      return;
    }

    setRemovingIds((prev) => {
      const next = new Set(prev);
      next.add(policyId);
      return next;
    });

    try {
      await toggleBookmark(policyId);
      setPolicies((prev) => prev.filter((p) => p.id !== policyId));
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("북마크를 취소하는 중 오류가 발생했습니다.");
      }
    } finally {
      setRemovingIds((prev) => {
        const next = new Set(prev);
        next.delete(policyId);
        return next;
      });
    }
  };

  if (authLoading || loading) {
    return (
      <main className="ds-page">
        <h1 className="ds-title">북마크한 정책</h1>
        <div className="ds-empty">로딩 중...</div>
      </main>
    );
  }

  if (!user) {
    return (
      <main className="ds-page">
        <h1 className="ds-title">북마크한 정책</h1>
        <div className="ds-empty">
          <p>북마크를 이용하려면 로그인이 필요합니다.</p>
          <Link href="/login" className="ds-link">
            로그인 페이지로 이동
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="ds-page">
      <h1 className="ds-title">북마크한 정책</h1>

      {error && <div className="ds-alert-error">{error}</div>}

      <div className="ds-count">북마크된 정책: {policies.length}건</div>

      {policies.length === 0 ? (
        <div className="ds-empty">북마크된 정책이 없습니다.</div>
      ) : (
        <div>
          {policies.map((policy, index) => {
            const key = policy.id ?? index;
            const policyId = policy.id;
            const isRemoving = policyId != null && removingIds.has(policyId);

            return (
              <div key={key} className="ds-card">
                <div className="ds-card-title">
                  {policy.plcyNm ?? "(정책명 없음)"}
                </div>
                <div className="ds-card-sub">
                  정책번호: {policy.plcyNo ?? "-"}
                </div>
                {policy.plcyExplnCn && (
                  <div className="ds-card-body">{policy.plcyExplnCn}</div>
                )}
                <div className="ds-card-meta">
                  {policy.sprtTrgtMinAge != null &&
                    policy.sprtTrgtMaxAge != null && (
                      <span>
                        연령: {policy.sprtTrgtMinAge}~{policy.sprtTrgtMaxAge}세
                      </span>
                    )}
                  {policy.plcyKywdNm && (
                    <span>{policy.sprtTrgtMinAge != null ? " | " : ""}태그: {policy.plcyKywdNm}</span>
                  )}
                </div>
                {policyId != null && (
                  <div className="ds-card-actions">
                    <button
                      type="button"
                      onClick={() => handleRemoveBookmark(policyId)}
                      disabled={isRemoving}
                      className="ds-btn ds-btn-danger ds-btn-sm"
                    >
                      {isRemoving ? "취소 중..." : "북마크 취소"}
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </main>
  );
}
