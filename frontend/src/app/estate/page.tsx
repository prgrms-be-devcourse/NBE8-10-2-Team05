"use client";

import { useState } from "react";
import { searchEstates } from "@/api/estate";
import { Estate } from "@/types/estate";

export default function EstatePage() {
  const [sido, setSido] = useState("");
  const [signguNm, setSignguNm] = useState("");
  const [estates, setEstates] = useState<Estate[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!sido || !signguNm) {
      alert("시/도와 시/군/구를 모두 입력해주세요.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await searchEstates({ sido, signguNm });
      setEstates(response.estateList);
    } catch (err: any) {
      setError(err.message || "검색 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h1>행복주택 검색</h1>
      
      <div style={{ marginBottom: "20px" }}>
        <input
          type="text"
          placeholder="시/도 (예: 서울특별시)"
          value={sido}
          onChange={(e) => setSido(e.target.value)}
          style={{ marginRight: "10px", padding: "5px" }}
        />
        <input
          type="text"
          placeholder="시/군/구 (예: 강남구)"
          value={signguNm}
          onChange={(e) => setSignguNm(e.target.value)}
          style={{ marginRight: "10px", padding: "5px" }}
        />
        <button onClick={handleSearch} disabled={loading} style={{ padding: "5px 10px" }}>
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div style={{ color: "red", marginBottom: "20px" }}>{error}</div>}

      <div>
        <h2>검색 결과 ({estates.length}건)</h2>
        {estates.length === 0 ? (
          <p>검색 결과가 없습니다.</p>
        ) : (
          <ul style={{ listStyle: "none", padding: 0 }}>
            {estates.map((estate) => (
              <li key={estate.id} style={{ border: "1px solid #ccc", margin: "10px 0", padding: "15px", borderRadius: "8px" }}>
                <h3 style={{ margin: "0 0 10px 0" }}>{estate.pblancNm}</h3>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px", fontSize: "0.9em" }}>
                  <p><strong>상태:</strong> {estate.sttusNm}</p>
                  <p><strong>공급기관:</strong> {estate.suplyInsttNm}</p>
                  <p><strong>유형:</strong> {estate.suplyTyNm} ({estate.houseTyNm})</p>
                  <p><strong>주소:</strong> {estate.fullAdres}</p>
                  <p><strong>모집일:</strong> {estate.rcritPblancDe} ~ {estate.endDe}</p>
                  <p><strong>임대료:</strong> 보증금 {estate.rentGtn?.toLocaleString()}원 / 월 {estate.mtRntchrg?.toLocaleString()}원</p>
                </div>
                {estate.url && (
                  <a href={estate.url} target="_blank" rel="noopener noreferrer" style={{ display: "inline-block", marginTop: "10px", color: "blue" }}>
                    상세 공고 보기
                  </a>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
