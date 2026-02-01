"use client";

import {SetStateAction, useEffect, useState} from "react";
import {searchEstates} from "@/api/estate";
import {Estate, EstateRegion} from "@/types/estate";
import {searchEstateRegions} from "@/api/estateRegion";

export default function EstatePage() {
    const [sido, setSido] = useState("");
    const [signguNm, setSignguNm] = useState("");
    const [estates, setEstates] = useState<Estate[]>([]);
    const [estateRegions, setEstateRegions] = useState<EstateRegion[]>([]);
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
            const response = await searchEstates({sido, signguNm});
            setEstates(response.estateList);
        } catch (err: any) {
            setError(err.message || "검색 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        // 내부에서 비동기 함수 정의
        const fetchRegions = async () => {
            try {
                setLoading(true); // 로딩 시작
                const response = await searchEstateRegions();
                setEstateRegions(response.estateRegionList || response);
            } catch (err : any) {
                setError(err.message || "지역을 받아오는 중 오류가 발생했습니다.");
            } finally {
                setLoading(false);
            }
        };
        fetchRegions(); // 호출
        setEstateRegions(dummyEstateRegions) //추후 삭제
    }, []); // 마운트 시 1회 실행


    const handleSidoChange = (e: { target: { value: SetStateAction<string>; }; }) => {
      setSido(e.target.value);
      setSignguNm(""); // 시군구 초기화
    };

    const sidoList = estateRegions.filter(r => r.level === 1);
    const gunguList = estateRegions.filter(r => r.level === 2 && r.parentName === sido);

    const dummyEstateRegions: EstateRegion[] = [
            // Level 1: 광역자치단체
            { name: "강원특별자치도", parentName: "", level: 1 },
            { name: "경기도", parentName: "", level: 1 },
            { name: "경상남도", parentName: "", level: 1 },
            { name: "경상북도", parentName: "", level: 1 },
            { name: "대구광역시", parentName: "", level: 1 },
            { name: "부산광역시", parentName: "", level: 1 },
            { name: "서울특별시", parentName: "", level: 1 },
            { name: "울산광역시", parentName: "", level: 1 },
            { name: "인천광역시", parentName: "", level: 1 },
            { name: "전라남도", parentName: "", level: 1 },
            { name: "전북특별자치도", parentName: "", level: 1 },
            { name: "충청남도", parentName: "", level: 1 },
            { name: "충청북도", parentName: "", level: 1 },

            // Level 2: 기초자치단체 (강원)
            { name: "강릉시", parentName: "강원특별자치도", level: 2 },
            { name: "동해시", parentName: "강원특별자치도", level: 2 },
            { name: "삼척시", parentName: "강원특별자치도", level: 2 },
            { name: "양양군", parentName: "강원특별자치도", level: 2 },
            { name: "정선군", parentName: "강원특별자치도", level: 2 },

            // Level 2: 기초자치단체 (경기)
            { name: "광명시", parentName: "경기도", level: 2 },
            { name: "광주시", parentName: "경기도", level: 2 },
            { name: "김포시", parentName: "경기도", level: 2 },
            { name: "안양시 동안구", parentName: "경기도", level: 2 },
            { name: "평택시", parentName: "경기도", level: 2 },

            // Level 2: 기타 지역
            { name: "김해시", parentName: "경상남도", level: 2 },
            { name: "영천시", parentName: "경상북도", level: 2 },
            { name: "달서구", parentName: "대구광역시", level: 2 },
            { name: "사상구", parentName: "부산광역시", level: 2 },
            { name: "강북구", parentName: "서울특별시", level: 2 },
            { name: "울주군", parentName: "울산광역시", level: 2 },
            { name: "남동구", parentName: "인천광역시", level: 2 },
            { name: "해남군", parentName: "전라남도", level: 2 },
            { name: "군산시", parentName: "전북특별자치도", level: 2 },
            { name: "당진시", parentName: "충청남도", level: 2 },
            { name: "진천군", parentName: "충청북도", level: 2 }
        ];

  return (
    <main className="ds-page">
      <h1 className="ds-title">행복주택 검색</h1>

      <div className="ds-search-bar">
        <div className="ds-form-group">
          <label className="ds-label">시/도</label>
          {/*<input*/}
          {/*  type="text"*/}
          {/*  className="ds-input ds-input-inline"*/}
          {/*  placeholder="예: 서울특별시"*/}
          {/*  value={sido}*/}
          {/*  onChange={(e) => setSido(e.target.value)}*/}
          {/*  style={{ width: "180px" }}*/}
          {/*/>*/}
            <select
                className="ds-input ds-input-inline"
                value={sido}
                onChange={handleSidoChange}
                style={{ width: "180px" }}
            >
                <option value="">전체</option>
                {sidoList.map(region => (
                    <option key={region.name} value={region.name}>{region.name}</option>
                ))}
            </select>

        </div>
        <div className="ds-form-group">
          <label className="ds-label">시/군/구</label>
          {/*<input*/}
          {/*  type="text"*/}
          {/*  className="ds-input ds-input-inline"*/}
          {/*  placeholder="예: 강남구"*/}
          {/*  value={signguNm}*/}
          {/*  onChange={(e) => setSignguNm(e.target.value)}*/}
          {/*  style={{ width: "180px" }}*/}
          {/*/>*/}
            <select
                className="ds-input ds-input-inline"
                value={signguNm}
                onChange={(e) => setSignguNm(e.target.value)}
                style={{ width: "180px" }}
                disabled={!sido} // 시/도를 먼저 선택해야 활성화
            >
                <option value="">전체</option>
                {gunguList.map(region => (
                    <option key={region.name} value={region.name}>{region.name}</option>
                ))}
            </select>
        </div>
        <button onClick={handleSearch} disabled={loading} className="ds-btn ds-btn-primary">
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div className="ds-alert-error">{error}</div>}

      <h2 className="ds-subtitle">검색 결과 ({estates.length}건)</h2>

      {estates.length === 0 ? (
        <div className="ds-empty">검색 결과가 없습니다.</div>
      ) : (
        <div>
          {estates.map((estate) => (
            <div key={estate.id} className="ds-list-card">
              <h3>{estate.pblancNm}</h3>
              <div className="ds-list-grid">
                <p><strong>상태:</strong> {estate.sttusNm}</p>
                <p><strong>공급기관:</strong> {estate.suplyInsttNm}</p>
                <p><strong>유형:</strong> {estate.suplyTyNm} ({estate.houseTyNm})</p>
                <p><strong>주소:</strong> {estate.fullAdres}</p>
                <p><strong>모집일:</strong> {estate.rcritPblancDe} ~ {estate.endDe}</p>
                <p><strong>임대료:</strong> 보증금 {estate.rentGtn?.toLocaleString()}원 / 월 {estate.mtRntchrg?.toLocaleString()}원</p>
              </div>
              {estate.url && (
                <a href={estate.url} target="_blank" rel="noopener noreferrer">
                  상세 공고 보기
                </a>
              )}
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
