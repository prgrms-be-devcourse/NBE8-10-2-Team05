"use client";

import { useState } from "react";
import type { PolicyDocument, PolicySearchRequest } from "@/types/policy";
import { searchPolicies } from "@/api/policy";

const PAGE_SIZE = 10;

export default function PolicySearch() {
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

  const handleSearch = async (fromValue: number = 0) => {
    setLoading(true);
    setError(null);
    setFrom(fromValue);

    try {
      const request = buildRequest(fromValue);
      const data = await searchPolicies(request);
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
            직업코드:{" "}
            <input
              type="text"
              value={jobCode}
              onChange={(e) => setJobCode(e.target.value)}
              placeholder="직업코드"
              style={{ width: "100px" }}
            />
          </label>

          <label>
            학력코드:{" "}
            <input
              type="text"
              value={schoolCode}
              onChange={(e) => setSchoolCode(e.target.value)}
              placeholder="학력코드"
              style={{ width: "100px" }}
            />
          </label>

          <label>
            결혼상태:{" "}
            <select
              value={marriageStatus}
              onChange={(e) => setMarriageStatus(e.target.value)}
            >
              <option value="">전체</option>
              <option value="SINGLE">미혼</option>
              <option value="MARRIED">기혼</option>
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
              placeholder="예: 주거, 취업, 교육"
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
          <div>검색 결과: {results.length}건</div>

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
