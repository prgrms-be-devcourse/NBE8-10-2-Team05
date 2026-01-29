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
    <div style={{ padding: "20px" }}>
      <h1>법률 상담소 검색</h1>
      
      <div style={{ marginBottom: "20px" }}>
        <input
          type="text"
          placeholder="시/도 (예: 서울특별시)"
          value={area1}
          onChange={(e) => setArea1(e.target.value)}
          style={{ marginRight: "10px", padding: "5px" }}
        />
        <input
          type="text"
          placeholder="시/군/구 (선택)"
          value={area2}
          onChange={(e) => setArea2(e.target.value)}
          style={{ marginRight: "10px", padding: "5px" }}
        />
        <button onClick={handleSearch} disabled={loading} style={{ padding: "5px 10px" }}>
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div style={{ color: "red", marginBottom: "20px" }}>{error}</div>}

      <div>
        <h2>검색 결과 ({lawyers.length}건)</h2>
        {lawyers.length === 0 ? (
          <p>검색 결과가 없습니다.</p>
        ) : (
          <ul style={{ listStyle: "none", padding: 0 }}>
            {lawyers.map((lawyer) => (
              <li key={lawyer.id} style={{ border: "1px solid #ccc", margin: "10px 0", padding: "10px" }}>
                <h3>{lawyer.name}</h3>
                <p><strong>법인명:</strong> {lawyer.corporation}</p>
                <p><strong>지역:</strong> {lawyer.districtArea1} {lawyer.districtArea2}</p>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
