"use client";

import { useState } from "react";
import { searchEstates, searchEstatesNoCache } from "@/api/estate";

export default function EstatePerformanceTestPage() {
  const [sido, setSido] = useState("서울");
  const [signguNm, setSignguNm] = useState("강남");
  
  const [dbTime, setDbTime] = useState<number | null>(null);
  const [redisTime, setRedisTime] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const testDbPerformance = async () => {
    setLoading(true);
    setError(null);
    setDbTime(null);
    try {
      const start = performance.now();
      await searchEstatesNoCache({ sido, signguNm });
      const end = performance.now();
      setDbTime(end - start);
    } catch (err: any) {
      setError(err.message || "DB 테스트 실패");
    } finally {
      setLoading(false);
    }
  };

  const testRedisPerformance = async () => {
    setLoading(true);
    setError(null);
    setRedisTime(null);
    try {
      const start = performance.now();
      await searchEstates({ sido, signguNm });
      const end = performance.now();
      setRedisTime(end - start);
    } catch (err: any) {
      setError(err.message || "Redis 테스트 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h1>행복주택 검색 성능 테스트</h1>
      
      <div style={{ marginBottom: "20px", padding: "15px", border: "1px solid #ddd", borderRadius: "8px", color: "white", backgroundColor: "#333" }}>
        <h3>테스트 조건 설정</h3>
        <div style={{ marginBottom: "10px" }}>
          <label style={{ marginRight: "10px" }}>시/도:</label>
          <input
            type="text"
            value={sido}
            onChange={(e) => setSido(e.target.value)}
            style={{ marginRight: "20px", padding: "5px", color: "white", backgroundColor: "#555", border: "1px solid #777" }}
          />
          <label style={{ marginRight: "10px" }}>시/군/구:</label>
          <input
            type="text"
            value={signguNm}
            onChange={(e) => setSignguNm(e.target.value)}
            style={{ padding: "5px", color: "white", backgroundColor: "#555", border: "1px solid #777" }}
          />
        </div>
      </div>

      {error && <div style={{ color: "red", marginBottom: "20px" }}>{error}</div>}

      <div style={{ display: "flex", gap: "20px" }}>
        {/* DB 테스트 영역 */}
        <div style={{ flex: 1, padding: "20px", border: "1px solid #ccc", borderRadius: "8px", backgroundColor: "#f9f9f9", color: "black" }}>
          <h2>DB 직접 조회 (No Cache)</h2>
          <p>캐시를 사용하지 않고 DB에서 직접 조회합니다.</p>
          <button 
            onClick={testDbPerformance} 
            disabled={loading}
            style={{ padding: "10px 20px", backgroundColor: "#ff6b6b", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}
          >
            테스트 실행
          </button>
          
          {dbTime !== null && (
            <div style={{ marginTop: "20px", fontSize: "1.5em", fontWeight: "bold", color: "#d63031" }}>
              소요 시간: {dbTime.toFixed(2)} ms
            </div>
          )}
        </div>

        {/* Redis 테스트 영역 */}
        <div style={{ flex: 1, padding: "20px", border: "1px solid #ccc", borderRadius: "8px", backgroundColor: "#e3f2fd", color: "black" }}>
          <h2>Redis 캐시 조회</h2>
          <p>Redis 캐시를 통해 조회합니다. (첫 조회 시에는 DB 조회 후 캐싱)</p>
          <button 
            onClick={testRedisPerformance} 
            disabled={loading}
            style={{ padding: "10px 20px", backgroundColor: "#0984e3", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}
          >
            테스트 실행
          </button>
          
          {redisTime !== null && (
            <div style={{ marginTop: "20px", fontSize: "1.5em", fontWeight: "bold", color: "#0984e3" }}>
              소요 시간: {redisTime.toFixed(2)} ms
            </div>
          )}
        </div>
      </div>
      
      <div style={{ marginTop: "30px", padding: "15px", backgroundColor: "#eee", borderRadius: "8px", color: "black" }}>
        <h3>💡 테스트 팁</h3>
        <ul>
          <li><strong>DB 조회:</strong> 항상 일정한 시간이 소요됩니다.</li>
          <li><strong>Redis 조회 (1차):</strong> Cache Miss 발생 시 DB 조회와 비슷하거나 약간 더 느릴 수 있습니다. (DB 조회 + Redis 저장)</li>
          <li><strong>Redis 조회 (2차 이상):</strong> Cache Hit 발생 시 <strong>매우 빠른 속도</strong>를 보여야 합니다.</li>
        </ul>
      </div>
    </div>
  );
}
