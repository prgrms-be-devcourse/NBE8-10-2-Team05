"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import type { ApplicationItem } from "@/types/policy";
import { getApplications, deleteApplication } from "@/api/application";
import { useAuth } from "@/contexts/AuthContext";

export default function ApplicationsPage() {
  const { user, isLoading: authLoading } = useAuth();

  const [applications, setApplications] = useState<ApplicationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancellingIds, setCancellingIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      setLoading(false);
      return;
    }

    const fetchApplications = async () => {
      try {
        const data = await getApplications();
        setApplications(data);
      } catch (err: unknown) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("신청 내역을 불러오는 중 오류가 발생했습니다.");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchApplications();
  }, [user, authLoading]);

  const handleCancel = async (applicationId: number) => {
    setCancellingIds((prev) => new Set(prev).add(applicationId));
    setError(null);

    try {
      await deleteApplication(applicationId);
      setApplications((prev) => prev.filter((a) => a.id !== applicationId));
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("신청 취소 중 오류가 발생했습니다.");
      }
    } finally {
      setCancellingIds((prev) => {
        const next = new Set(prev);
        next.delete(applicationId);
        return next;
      });
    }
  };

  if (authLoading || loading) {
    return (
      <main className="ds-page">
        <h1 className="ds-title">정책 신청내역</h1>
        <div className="ds-empty">로딩 중...</div>
      </main>
    );
  }

  if (!user) {
    return (
      <main className="ds-page">
        <h1 className="ds-title">정책 신청내역</h1>
        <div className="ds-empty">
          <p>신청내역을 확인하려면 로그인이 필요합니다.</p>
          <Link href="/login" className="ds-link">로그인 페이지로 이동</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="ds-page">
      <h1 className="ds-title">정책 신청내역</h1>

      {error && <div className="ds-alert-error">{error}</div>}

      <div className="ds-count">신청된 정책: {applications.length}건</div>

      {applications.length === 0 ? (
        <div className="ds-empty">신청한 정책이 없습니다.</div>
      ) : (
        <div>
          {applications.map((app) => (
            <div key={app.id} className="ds-card">
              <div className="ds-card-title">
                {app.policy?.plcyNm ?? "(정책명 없음)"}
              </div>
              <div className="ds-card-sub">
                정책번호: {app.policy?.plcyNo ?? "-"}
              </div>
              {app.policy?.plcyExplnCn && (
                <div className="ds-card-body">{app.policy.plcyExplnCn}</div>
              )}
              <div className="ds-card-meta">
                <span>
                  신청일: {app.createdAt ? new Date(app.createdAt).toLocaleDateString() : "-"}
                </span>
              </div>
              <div className="ds-card-actions">
                <button
                  type="button"
                  onClick={() => handleCancel(app.id)}
                  disabled={cancellingIds.has(app.id)}
                  className="ds-btn ds-btn-danger ds-btn-sm"
                >
                  {cancellingIds.has(app.id) ? "취소 중..." : "신청 취소"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
