"use client";

import { useState, useCallback } from "react";
import type { PolicyDocument, PolicySearchRequest } from "@/types/policy";
import { JobCodeLabel, SchoolCodeLabel, MarriageStatusCodeLabel } from "@/types/policy";
import { searchPolicies } from "@/api/policy";
import { getBookmarks, toggleBookmark } from "@/api/bookmark";
import { getApplications, addApplication } from "@/api/application";
import { useAuth } from "@/contexts/AuthContext";

const PAGE_SIZE = 10;

export default function PolicySearch() {
  const { user } = useAuth();

  // 검색 폼 상태
  const [keyword, setKeyword] = useState("");
  const [age, setAge] = useState("");
  const [earn, setEarn] = useState("");
  const [regionCode, setRegionCode] = useState("");
  const [jobCode, setJobCode] = useState("");
  const [schoolCode, setSchoolCode] = useState("");
  const [marriageStatus, setMarriageStatus] = useState("");
  const [keywordsInput, setKeywordsInput] = useState("");

  // 결과 상태
  const [results, setResults] = useState<PolicyDocument[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searched, setSearched] = useState(false);

  // 페이징
  const [from, setFrom] = useState(0);

  // 북마크 상태: policyId Set
  const [bookmarkedIds, setBookmarkedIds] = useState<Set<number>>(new Set());
  const [togglingIds, setTogglingIds] = useState<Set<number>>(new Set());

  // 신청 상태: policyId Set
  const [appliedIds, setAppliedIds] = useState<Set<number>>(new Set());
  const [applyingIds, setApplyingIds] = useState<Set<number>>(new Set());

  const buildRequest = (fromValue: number): PolicySearchRequest => {
    return {
      keyword: keyword || null,
      age: age ? Number(age) : null,
      earn: earn ? Number(earn) : null,
      regionCode: regionCode || null,
      jobCode: jobCode || null,
      schoolCode: schoolCode || null,
      marriageStatus: marriageStatus || null,
      keywords:
        keywordsInput.trim()
          ? keywordsInput.split(",").map((k) => k.trim()).filter(Boolean)
          : null,
      from: fromValue,
      size: PAGE_SIZE,
    };
  };

  const fetchBookmarkedIds = useCallback(async () => {
    if (!user) return;
    try {
      const data = await getBookmarks();
      if (data.code === 200 && data.policies) {
        const ids = new Set<number>();
        for (const p of data.policies) {
          if (p.id != null) ids.add(p.id);
        }
        setBookmarkedIds(ids);
      }
    } catch {
      // 북마크 조회 실패해도 검색은 계속 진행
    }
  }, [user]);

  const fetchAppliedIds = useCallback(async () => {
    if (!user) return;
    try {
      const data = await getApplications();
      const ids = new Set<number>();
      for (const app of data) {
        if (app.policy?.id != null) ids.add(app.policy.id);
      }
      setAppliedIds(ids);
    } catch {
      // 신청 목록 조회 실패해도 검색은 계속 진행
    }
  }, [user]);

  const handleSearch = async (fromValue: number = 0) => {
    setLoading(true);
    setError(null);
    setFrom(fromValue);

    try {
      const request = buildRequest(fromValue);
      const [data] = await Promise.all([
        searchPolicies(request),
        fetchBookmarkedIds(),
        fetchAppliedIds(),
      ]);
      setResults(data);
      setSearched(true);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("검색 중 오류가 발생했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleToggleBookmark = async (policyId: number) => {
    if (!user) {
      setError("북마크를 사용하려면 로그인이 필요합니다.");
      return;
    }

    // 이미 북마크된 경우, 검색 결과 화면에서는 취소를 허용하지 않음
    if (bookmarkedIds.has(policyId)) {
      return;
    }

    setTogglingIds((prev) => {
      const next = new Set(prev);
      next.add(policyId);
      return next;
    });
    try {
      await toggleBookmark(policyId);
      setBookmarkedIds((prev) => {
        const next = new Set(prev);
        next.add(policyId);
        return next;
      });
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("북마크 처리 중 오류가 발생했습니다.");
      }
    } finally {
      setTogglingIds((prev) => {
        const next = new Set(prev);
        next.delete(policyId);
        return next;
      });
    }
  };

  const handleApply = async (policyId: number) => {
    if (!user) {
      setError("신청하려면 로그인이 필요합니다.");
      return;
    }

    setApplyingIds((prev) => new Set(prev).add(policyId));
    try {
      await addApplication(policyId);
      setAppliedIds((prev) => new Set(prev).add(policyId));
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("신청 처리 중 오류가 발생했습니다.");
      }
    } finally {
      setApplyingIds((prev) => {
        const next = new Set(prev);
        next.delete(policyId);
        return next;
      });
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearch(0);
  };

  const handleNextPage = () => {
    handleSearch(from + PAGE_SIZE);
  };

  const handlePrevPage = () => {
    const newFrom = Math.max(0, from - PAGE_SIZE);
    handleSearch(newFrom);
  };

  return (
    <div>
      {/* 검색 폼 */}
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: "12px" }}>
          <label>
            키워드:{" "}
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="검색어 입력"
            />
          </label>
        </div>

        <div style={{ display: "flex", gap: "12px", flexWrap: "wrap", marginBottom: "12px" }}>
          <label>
            나이:{" "}
            <input
              type="number"
              value={age}
              onChange={(e) => setAge(e.target.value)}
              placeholder="나이"
              style={{ width: "80px" }}
            />
          </label>

          <label>
            소득(만원):{" "}
            <input
              type="number"
              value={earn}
              onChange={(e) => setEarn(e.target.value)}
              placeholder="소득"
              style={{ width: "100px" }}
            />
          </label>

          <label>
            지역코드:{" "}
            <input
              type="text"
              value={regionCode}
              onChange={(e) => setRegionCode(e.target.value)}
              placeholder="지역코드"
              style={{ width: "100px" }}
            />
          </label>

          <label>
            취업상태:{" "}
            <select
              value={jobCode}
              onChange={(e) => setJobCode(e.target.value)}
            >
              <option value="">전체</option>
              {Object.entries(JobCodeLabel).map(([code, label]) => (
                <option key={code} value={code}>{label}</option>
              ))}
            </select>
          </label>

          <label>
            학력:{" "}
            <select
              value={schoolCode}
              onChange={(e) => setSchoolCode(e.target.value)}
            >
              <option value="">전체</option>
              {Object.entries(SchoolCodeLabel).map(([code, label]) => (
                <option key={code} value={code}>{label}</option>
              ))}
            </select>
          </label>

          <label>
            결혼상태:{" "}
            <select
              value={marriageStatus}
              onChange={(e) => setMarriageStatus(e.target.value)}
            >
              <option value="">전체</option>
              {Object.entries(MarriageStatusCodeLabel).map(([code, label]) => (
                <option key={code} value={code}>{label}</option>
              ))}
            </select>
          </label>
        </div>

        <div style={{ marginBottom: "12px" }}>
          <label>
            태그 (쉼표 구분):{" "}
            <input
              type="text"
              value={keywordsInput}
              onChange={(e) => setKeywordsInput(e.target.value)}
              placeholder="예: 주거지원, 보조금"
              style={{ width: "300px" }}
            />
          </label>
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "검색 중..." : "검색"}
        </button>
      </form>

      {/* 에러 */}
      {error && (
        <div style={{ color: "red", marginTop: "12px" }}>
          오류: {error}
        </div>
      )}

      {/* 검색 결과 */}
      {searched && !loading && (
        <div style={{ marginTop: "20px" }}>
          {results.length === 0 ? (
            <div style={{ marginTop: "8px" }}>검색 결과가 없습니다.</div>
          ) : (
            <div>
              {results.map((policy, index) => (
                <div
                  key={policy.policyId ?? index}
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
                  {policy.description && (
                    <div style={{ marginTop: "4px" }}>{policy.description}</div>
                  )}
                  <div style={{ marginTop: "4px", fontSize: "14px", color: "#666" }}>
                    {policy.minAge != null && policy.maxAge != null && (
                      <span>연령: {policy.minAge}~{policy.maxAge}세 | </span>
                    )}
                    {policy.regionCode && <span>지역: {policy.regionCode} | </span>}
                    {policy.marriageStatus && <span>결혼: {policy.marriageStatus} | </span>}
                    {policy.keywords && policy.keywords.length > 0 && (
                      <span>태그: {policy.keywords.join(", ")}</span>
                    )}
                  </div>
                  <div style={{ marginTop: "8px", display: "flex", gap: "8px" }}>
                    {policy.policyId != null && (
                      <button
                        type="button"
                        onClick={() => handleToggleBookmark(policy.policyId!)}
                        disabled={
                          togglingIds.has(policy.policyId) ||
                          bookmarkedIds.has(policy.policyId)
                        }
                        style={{
                          padding: "4px 12px",
                          cursor:
                            togglingIds.has(policy.policyId) ||
                            bookmarkedIds.has(policy.policyId)
                              ? "not-allowed"
                              : "pointer",
                          backgroundColor: bookmarkedIds.has(policy.policyId)
                            ? "#333"
                            : "#fff",
                          color: bookmarkedIds.has(policy.policyId) ? "#fff" : "#333",
                          border: "1px solid #333",
                        }}
                      >
                        {togglingIds.has(policy.policyId)
                          ? "처리중..."
                          : bookmarkedIds.has(policy.policyId)
                            ? "북마크 완료"
                            : "북마크"}
                      </button>
                    )}
                    {policy.policyId != null && (
                      <button
                        type="button"
                        onClick={() => handleApply(policy.policyId!)}
                        disabled={applyingIds.has(policy.policyId) || appliedIds.has(policy.policyId)}
                        style={{
                          padding: "4px 12px",
                          cursor:
                            applyingIds.has(policy.policyId) || appliedIds.has(policy.policyId)
                              ? "not-allowed"
                              : "pointer",
                          backgroundColor: appliedIds.has(policy.policyId) ? "#333" : "#fff",
                          color: appliedIds.has(policy.policyId) ? "#fff" : "#333",
                          border: "1px solid #333",
                        }}
                      >
                        {applyingIds.has(policy.policyId)
                          ? "처리중..."
                          : appliedIds.has(policy.policyId)
                            ? "신청완료"
                            : "신청"}
                      </button>
                    )}
                  </div>
                </div>
              ))}

              {/* 페이징 */}
              <div style={{ marginTop: "12px", display: "flex", gap: "8px" }}>
                <button onClick={handlePrevPage} disabled={from === 0 || loading}>
                  이전
                </button>
                <span>페이지 {Math.floor(from / PAGE_SIZE) + 1}</span>
                <button
                  onClick={handleNextPage}
                  disabled={results.length < PAGE_SIZE || loading}
                >
                  다음
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
