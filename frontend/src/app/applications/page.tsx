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
      <main style={{ padding: "20px" }}>
        <h1>정책 신청내역</h1>
        <div>로딩 중...</div>
      </main>
    );
  }

  if (!user) {
    return (
      <main style={{ padding: "20px" }}>
        <h1>정책 신청내역</h1>
        <p>신청내역을 확인하려면 로그인이 필요합니다.</p>
        <Link href="/login">로그인 페이지로 이동</Link>
      </main>
    );
  }

  return (
    <main style={{ padding: "20px" }}>
      <h1>정책 신청내역</h1>

      {error && (
        <div style={{ color: "red", marginTop: "12px" }}>오류: {error}</div>
      )}

      <div style={{ marginTop: "12px" }}>
        <div>신청된 정책: {applications.length}건</div>

        {applications.length === 0 ? (
          <div style={{ marginTop: "8px" }}>신청한 정책이 없습니다.</div>
        ) : (
          <div>
            {applications.map((app) => (
              <div
                key={app.id}
                style={{
                  border: "1px solid #ccc",
                  padding: "12px",
                  marginTop: "8px",
                }}
              >
                <div>
                  <strong>
                    {app.policy?.plcyNm ?? "(정책명 없음)"}
                  </strong>
                </div>
                <div>정책번호: {app.policy?.plcyNo ?? "-"}</div>
                {app.policy?.plcyExplnCn && (
                  <div style={{ marginTop: "4px" }}>
                    {app.policy.plcyExplnCn}
                  </div>
                )}
                <div
                  style={{
                    marginTop: "4px",
                    fontSize: "14px",
                    color: "#666",
                  }}
                >
                  신청일: {app.createdAt ? new Date(app.createdAt).toLocaleDateString() : "-"}
                </div>
                <div style={{ marginTop: "8px" }}>
                  <button
                    type="button"
                    onClick={() => handleCancel(app.id)}
                    disabled={cancellingIds.has(app.id)}
                    style={{
                      padding: "4px 12px",
                      cursor: cancellingIds.has(app.id)
                        ? "not-allowed"
                        : "pointer",
                      backgroundColor: "#fff",
                      color: "#c00",
                      border: "1px solid #c00",
                    }}
                  >
                    {cancellingIds.has(app.id) ? "취소 중..." : "신청 취소"}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}
