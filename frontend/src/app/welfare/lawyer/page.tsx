"use client";

import { useState } from "react";
import { searchLawyers } from "@/api/center";
import { LawyerResponse } from "@/types/center";

export default function LawyerSearchPage() {
  const [area1, setArea1] = useState("");
  const [area2, setArea2] = useState("");
  const [lawyers, setLawyers] = useState<LawyerResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!area1) {
      alert("시/도를 입력해주세요.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      // 페이지네이션은 일단 0페이지, 100개 사이즈로 고정하여 전체 조회 느낌으로 구현
      const response = await searchLawyers({ area1, area2, page: 0, size: 100 });
      setLawyers(response.content);
    } catch (err: any) {
      setError(err.message || "검색 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="ds-page">
      <h1 className="ds-title">법률 상담소 검색</h1>

      <div className="ds-search-bar">
        <div className="ds-form-group">
          <label className="ds-label">시/도</label>
          <input
            type="text"
            className="ds-input ds-input-inline"
            placeholder="예: 서울특별시"
            value={area1}
            onChange={(e) => setArea1(e.target.value)}
            style={{ width: "180px" }}
          />
        </div>
        <div className="ds-form-group">
          <label className="ds-label">시/군/구 (선택)</label>
          <input
            type="text"
            className="ds-input ds-input-inline"
            placeholder="시/군/구"
            value={area2}
            onChange={(e) => setArea2(e.target.value)}
            style={{ width: "180px" }}
          />
        </div>
        <button onClick={handleSearch} disabled={loading} className="ds-btn ds-btn-primary">
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div className="ds-alert-error">{error}</div>}

      <h2 className="ds-subtitle">검색 결과 ({lawyers.length}건)</h2>

      {lawyers.length === 0 ? (
        <div className="ds-empty">검색 결과가 없습니다.</div>
      ) : (
        <div>
          {lawyers.map((lawyer) => (
            <div key={lawyer.id} className="ds-list-card">
              <h3>{lawyer.name}</h3>
              <p><strong>법인명:</strong> {lawyer.corporation}</p>
              <p><strong>지역:</strong> {lawyer.districtArea1} {lawyer.districtArea2}</p>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
