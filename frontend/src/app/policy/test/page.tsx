"use client";

import { useState } from "react";
import { searchPolicies, searchPolicyDb, fetchPolicies } from "@/api/policy";
import { startBatchJob } from "@/api/batch";

export default function PolicyPerformanceTestPage() {
  const [age, setAge] = useState<string>("25");
  
  const [dbTime, setDbTime] = useState<number | null>(null);
  const [esTime, setEsTime] = useState<number | null>(null);
  const [apiFetchTime, setApiFetchTime] = useState<number | null>(null);
  const [batchFetchTime, setBatchFetchTime] = useState<number | null>(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleApiFetch = async () => {
    if (!confirm("일반 API로 데이터 적재를 시작하시겠습니까? 백그라운드에서 실행됩니다.")) return;
    try {
      const start = performance.now();
      const message = await fetchPolicies();
      const end = performance.now();
      setApiFetchTime(end - start);
      alert(message);
    } catch (err: any) {
      alert(err.message || "데이터 적재 요청에 실패했습니다.");
    }
  };

  const handleBatchFetch = async () => {
    if (!confirm("Spring Batch로 데이터 적재를 시작하시겠습니까? 백그라운드에서 실행됩니다.")) return;
    try {
      const start = performance.now();
      const message = await startBatchJob();
      const end = performance.now();
      setBatchFetchTime(end - start);
      alert(message);
    } catch (err: any) {
      alert(err.message || "배치 실행 요청에 실패했습니다.");
    }
  };

  const testDbPerformance = async () => {
    setLoading(true);
    setError(null);
    setDbTime(null);
    try {
      const start = performance.now();
      await searchPolicyDb({ sprtTrgtMinAge: Number(age) });
      const end = performance.now();
      setDbTime(end - start);
    } catch (err: any) {
      setError(err.message || "DB 테스트 실패");
    } finally {
      setLoading(false);
    }
  };

  const testEsPerformance = async () => {
    setLoading(true);
    setError(null);
    setEsTime(null);
    try {
      const start = performance.now();
      await searchPolicies({ age: Number(age) });
      const end = performance.now();
      setEsTime(end - start);
    } catch (err: any) {
      setError(err.message || "ES 테스트 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h1>성능 테스트 페이지</h1>
      
      <div style={{ marginBottom: "20px", padding: "15px", border: "1px solid #ddd", borderRadius: "8px", color: "white", backgroundColor: "#333" }}>
        <h3>테스트 조건 설정</h3>
        <div style={{ marginBottom: "10px" }}>
          <label style={{ marginRight: "10px" }}>나이:</label>
          <input
            type="number"
            value={age}
            onChange={(e) => setAge(e.target.value)}
            style={{ padding: "5px", color: "white", backgroundColor: "#555", border: "1px solid #777" }}
          />
        </div>
      </div>

      {error && <div style={{ color: "red", marginBottom: "20px" }}>{error}</div>}

      <div style={{ display: "flex", gap: "20px", marginBottom: "20px" }}>
        {/* 데이터 적재 테스트 */}
        <div style={{ flex: 1, padding: "20px", border: "1px solid #ccc", borderRadius: "8px", backgroundColor: "#f0f0f0", color: "black" }}>
          <h2>데이터 적재 테스트</h2>
          <p>일반 API 호출과 Spring Batch Job 실행의 요청-응답 시간을 비교합니다.</p>
          <div style={{ display: "flex", gap: "10px", marginTop: "10px" }}>
            <button onClick={handleApiFetch} disabled={loading} style={{ padding: "10px", backgroundColor: "#e67e22", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
              일반 API로 적재
            </button>
            <button onClick={handleBatchFetch} disabled={loading} style={{ padding: "10px", backgroundColor: "#3498db", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
              Spring Batch로 적재
            </button>
          </div>
          {apiFetchTime !== null && <p><strong>API 호출 응답 시간:</strong> {apiFetchTime.toFixed(2)} ms</p>}
          {batchFetchTime !== null && <p><strong>Batch Job 응답 시간:</strong> {batchFetchTime.toFixed(2)} ms</p>}
        </div>
      </div>

      <div style={{ display: "flex", gap: "20px" }}>
        {/* 검색 성능 테스트 */}
        <div style={{ flex: 1, padding: "20px", border: "1px solid #ccc", borderRadius: "8px", backgroundColor: "#f9f9f9", color: "black" }}>
          <h2>DB 조회 (JPA/QueryDSL)</h2>
          <button onClick={testDbPerformance} disabled={loading} style={{ padding: "10px 20px", backgroundColor: "#ff6b6b", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
            테스트 실행
          </button>
          {dbTime !== null && <div style={{ marginTop: "20px", fontSize: "1.5em", fontWeight: "bold", color: "#d63031" }}>소요 시간: {dbTime.toFixed(2)} ms</div>}
        </div>
        <div style={{ flex: 1, padding: "20px", border: "1px solid #ccc", borderRadius: "8px", backgroundColor: "#e3f2fd", color: "black" }}>
          <h2>Elasticsearch 검색</h2>
          <button onClick={testEsPerformance} disabled={loading} style={{ padding: "10px 20px", backgroundColor: "#0984e3", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
            테스트 실행
          </button>
          {esTime !== null && <div style={{ marginTop: "20px", fontSize: "1.5em", fontWeight: "bold", color: "#0984e3" }}>소요 시간: {esTime.toFixed(2)} ms</div>}
        </div>
      </div>
    </div>
  );
}
