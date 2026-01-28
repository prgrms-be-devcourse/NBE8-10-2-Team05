"use client";

import { useState, useEffect } from "react";
import Header from "@/components/Header";
import { useAuth } from "@/contexts/AuthContext";
import { getMemberDetail, ApiError } from "@/api/member";
import { fetchEstateList } from "@/api/estate";
import type { EstateItem, EstateSearchParams } from "@/types/estate";

function getCurrentYearMonth(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  return `${year}${month}`;
}

export default function EstatePage() {
  const { user } = useAuth();

  // 검색 필터 상태
  const [signguCode, setSignguCode] = useState<string>("");
  const [suplyTy, setSuplyTy] = useState<string>("");
  const [houseTy, setHouseTy] = useState<string>("");
  const [lfstsTyAt, setLfstsTyAt] = useState<string>("");
  const [bassMtRntchrgSe, setBassMtRntchrgSe] = useState<string>("");

  // 결과 & UI 상태
  const [items, setItems] = useState<EstateItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false);

  // 로그인한 유저의 regionCode를 signguCode 기본값으로 설정
  useEffect(() => {
    if (!user) return;
    getMemberDetail()
      .then((detail) => {
        if (detail.regionCode) {
          setSignguCode(detail.regionCode);
        }
      })
      .catch((err) => {
        console.error("회원 상세 조회 실패:", err);
      });
  }, [user]);

  const handleSearch = async () => {
    setError(null);
    setIsLoading(true);
    setHasSearched(true);

    const currentYm = getCurrentYearMonth();

    const params: EstateSearchParams = {
      signguCode: signguCode || null,
      suplyTy: suplyTy || null,
      houseTy: houseTy || null,
      lfstsTyAt: lfstsTyAt || null,
      bassMtRntchrgSe: bassMtRntchrgSe || null,
      yearMtBegin: null,
      yearMtEnd: currentYm,
    };

    try {
      const data = await fetchEstateList(params);
      // 현재 날짜 기준으로 모집 기간이 유효한 항목만 필터링
      const today = new Date();
      const todayStr =
        String(today.getFullYear()) +
        String(today.getMonth() + 1).padStart(2, "0") +
        String(today.getDate()).padStart(2, "0");

      const filtered = data.filter((item) => {
        if (item.endDe && item.endDe < todayStr) return false;
        return true;
      });
      setItems(filtered);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(`[${err.resultCode}] ${err.msg}`);
      } else {
        setError("행복주택 목록을 불러오는 중 오류가 발생했습니다.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const formatMoney = (value: number | null) => {
    if (value == null) return "-";
    return value.toLocaleString() + "원";
  };

  return (
    <div>
      <Header />
      <main style={{ padding: "20px" }}>
        <h1>행복주택 조회</h1>

        {/* 검색 필터 */}
        <div
          style={{
            border: "1px solid #ddd",
            padding: "16px",
            borderRadius: "8px",
            marginBottom: "20px",
          }}
        >
          <h2 style={{ marginTop: 0 }}>검색 조건</h2>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "12px",
            }}
          >
            <div>
              <label>시군구 코드</label>
              <br />
              <input
                type="text"
                value={signguCode}
                onChange={(e) => setSignguCode(e.target.value)}
                placeholder="예: 11680 (서울 강남구)"
                style={{ width: "100%", padding: "6px" }}
              />
            </div>

            <div>
              <label>공급유형</label>
              <br />
              <select
                value={suplyTy}
                onChange={(e) => setSuplyTy(e.target.value)}
                style={{ width: "100%", padding: "6px" }}
              >
                <option value="">전체</option>
                <option value="06">행복주택</option>
                <option value="04">국민임대</option>
                <option value="05">영구임대</option>
                <option value="07">장기전세</option>
                <option value="08">공공임대</option>
                <option value="09">전세임대</option>
                <option value="10">통합공공임대</option>
              </select>
            </div>

            <div>
              <label>주택유형</label>
              <br />
              <select
                value={houseTy}
                onChange={(e) => setHouseTy(e.target.value)}
                style={{ width: "100%", padding: "6px" }}
              >
                <option value="">전체</option>
                <option value="01">아파트</option>
                <option value="02">오피스텔</option>
                <option value="03">다가구</option>
              </select>
            </div>

            <div>
              <label>전세형 모집 여부</label>
              <br />
              <select
                value={lfstsTyAt}
                onChange={(e) => setLfstsTyAt(e.target.value)}
                style={{ width: "100%", padding: "6px" }}
              >
                <option value="">전체</option>
                <option value="Y">예</option>
                <option value="N">아니오</option>
              </select>
            </div>

            <div>
              <label>월임대료 구분</label>
              <br />
              <select
                value={bassMtRntchrgSe}
                onChange={(e) => setBassMtRntchrgSe(e.target.value)}
                style={{ width: "100%", padding: "6px" }}
              >
                <option value="">전체</option>
                <option value="1">1구간</option>
                <option value="2">2구간</option>
                <option value="3">3구간</option>
              </select>
            </div>
          </div>

          <div style={{ marginTop: "16px" }}>
            <button
              type="button"
              onClick={handleSearch}
              disabled={isLoading}
              style={{ padding: "8px 24px", cursor: "pointer" }}
            >
              {isLoading ? "검색 중..." : "검색"}
            </button>
          </div>
        </div>

        {/* 에러 메시지 */}
        {error && (
          <div style={{ color: "red", marginBottom: "10px" }}>{error}</div>
        )}

        {/* 결과 목록 */}
        {hasSearched && !isLoading && (
          <div>
            <h2>검색 결과 ({items.length}건)</h2>
            {items.length === 0 ? (
              <p>검색 결과가 없습니다.</p>
            ) : (
              <table
                style={{
                  width: "100%",
                  borderCollapse: "collapse",
                  fontSize: "14px",
                }}
              >
                <thead>
                  <tr
                    style={{
                      borderBottom: "2px solid #333",
                      textAlign: "left",
                    }}
                  >
                    <th style={{ padding: "8px" }}>공고명</th>
                    <th style={{ padding: "8px" }}>상태</th>
                    <th style={{ padding: "8px" }}>공급유형</th>
                    <th style={{ padding: "8px" }}>주택유형</th>
                    <th style={{ padding: "8px" }}>지역</th>
                    <th style={{ padding: "8px" }}>공급호수</th>
                    <th style={{ padding: "8px" }}>보증금</th>
                    <th style={{ padding: "8px" }}>월임대료</th>
                    <th style={{ padding: "8px" }}>모집기간</th>
                    <th style={{ padding: "8px" }}>상세</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((item, idx) => (
                    <tr
                      key={`${item.pblancId}-${item.houseSn}-${idx}`}
                      style={{ borderBottom: "1px solid #ddd" }}
                    >
                      <td style={{ padding: "8px" }}>{item.pblancNm}</td>
                      <td style={{ padding: "8px" }}>{item.sttusNm}</td>
                      <td style={{ padding: "8px" }}>{item.suplyTyNm}</td>
                      <td style={{ padding: "8px" }}>{item.houseTyNm}</td>
                      <td style={{ padding: "8px" }}>
                        {item.brtcNm} {item.signguNm}
                      </td>
                      <td style={{ padding: "8px" }}>{item.suplyHoCo}</td>
                      <td style={{ padding: "8px" }}>
                        {formatMoney(item.rentGtn)}
                      </td>
                      <td style={{ padding: "8px" }}>
                        {formatMoney(item.mtRntchrg)}
                      </td>
                      <td style={{ padding: "8px" }}>
                        {item.beginDe} ~ {item.endDe}
                      </td>
                      <td style={{ padding: "8px" }}>
                        {item.url && (
                          <a
                            href={item.url}
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            바로가기
                          </a>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
