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
        <div className="ds-form-group">
          <label className="ds-label">키워드</label>
          <input
            type="text"
            className="ds-input"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="검색어 입력"
          />
        </div>

        <div className="ds-search-bar">
          <div className="ds-form-group">
            <label className="ds-label">나이</label>
            <input
              type="number"
              className="ds-input ds-input-inline"
              value={age}
              onChange={(e) => setAge(e.target.value)}
              placeholder="나이"
              style={{ width: "90px" }}
            />
          </div>

          <div className="ds-form-group">
            <label className="ds-label">소득(만원)</label>
            <input
              type="number"
              className="ds-input ds-input-inline"
              value={earn}
              onChange={(e) => setEarn(e.target.value)}
              placeholder="소득"
              style={{ width: "110px" }}
            />
          </div>

          <div className="ds-form-group">
            <label className="ds-label">지역코드</label>
            <input
              type="text"
              className="ds-input ds-input-inline"
              value={regionCode}
              onChange={(e) => setRegionCode(e.target.value)}
              placeholder="지역코드"
              style={{ width: "110px" }}
            />
          </div>

          <div className="ds-form-group">
            <label className="ds-label">취업상태</label>
            <select
              className="ds-select ds-input-inline"
              value={jobCode}
              onChange={(e) => setJobCode(e.target.value)}
              style={{ width: "130px" }}
            >
              <option value="">전체</option>
              {Object.entries(JobCodeLabel).map(([code, label]) => (
                <option key={code} value={code}>{label}</option>
              ))}
            </select>
          </div>

          <div className="ds-form-group">
            <label className="ds-label">학력</label>
            <select
              className="ds-select ds-input-inline"
              value={schoolCode}
              onChange={(e) => setSchoolCode(e.target.value)}
              style={{ width: "130px" }}
            >
              <option value="">전체</option>
              {Object.entries(SchoolCodeLabel).map(([code, label]) => (
                <option key={code} value={code}>{label}</option>
              ))}
            </select>
          </div>

          <div className="ds-form-group">
            <label className="ds-label">결혼상태</label>
            <select
              className="ds-select ds-input-inline"
              value={marriageStatus}
              onChange={(e) => setMarriageStatus(e.target.value)}
              style={{ width: "120px" }}
            >
              <option value="">전체</option>
              {Object.entries(MarriageStatusCodeLabel).map(([code, label]) => (
                <option key={code} value={code}>{label}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="ds-form-group">
          <label className="ds-label">태그 (쉼표 구분)</label>
          <input
            type="text"
            className="ds-input"
            value={keywordsInput}
            onChange={(e) => setKeywordsInput(e.target.value)}
            placeholder="예: 주거지원, 보조금"
          />
        </div>

        <button type="submit" disabled={loading} className="ds-btn ds-btn-primary">
          {loading ? "검색 중..." : "검색"}
        </button>
      </form>

      {/* 에러 */}
      {error && <div className="ds-alert-error" style={{ marginTop: "16px" }}>{error}</div>}

      {/* 검색 결과 */}
      {searched && !loading && (
        <div style={{ marginTop: "24px" }}>
          {results.length === 0 ? (
            <div className="ds-empty">검색 결과가 없습니다.</div>
          ) : (
            <div>
              {results.map((policy, index) => (
                <div
                  key={policy.policyId ?? index}
                  className="ds-card"
                >
                  <div className="ds-card-title">
                    {policy.plcyNm ?? "(정책명 없음)"}
                  </div>
                  <div className="ds-card-sub">정책번호: {policy.plcyNo ?? "-"}</div>
                  {policy.description && (
                    <div className="ds-card-body">{policy.description}</div>
                  )}
                  <div className="ds-card-meta">
                    {policy.minAge != null && policy.maxAge != null && (
                      <span>연령: {policy.minAge}~{policy.maxAge}세</span>
                    )}
                    {policy.keywords && policy.keywords.length > 0 && (
                      <span>태그: {policy.keywords.join(", ")}</span>
                    )}
                  </div>
                  <div className="ds-card-actions">
                    {policy.policyId != null && (
                      <button
                        type="button"
                        onClick={() => handleToggleBookmark(policy.policyId!)}
                        disabled={
                          togglingIds.has(policy.policyId) ||
                          bookmarkedIds.has(policy.policyId)
                        }
                        className={`ds-btn ds-btn-sm ${
                          bookmarkedIds.has(policy.policyId)
                            ? "ds-btn-done"
                            : "ds-btn-secondary"
                        }`}
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
                        className={`ds-btn ds-btn-sm ${
                          appliedIds.has(policy.policyId)
                            ? "ds-btn-done"
                            : "ds-btn-primary"
                        }`}
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
              <div className="ds-pagination">
                <button
                  onClick={handlePrevPage}
                  disabled={from === 0 || loading}
                  className="ds-btn ds-btn-secondary ds-btn-sm"
                >
                  이전
                </button>
                <span>페이지 {Math.floor(from / PAGE_SIZE) + 1}</span>
                <button
                  onClick={handleNextPage}
                  disabled={results.length < PAGE_SIZE || loading}
                  className="ds-btn ds-btn-secondary ds-btn-sm"
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
