"use client";

import { useState, useEffect } from "react";
import Script from "next/script";
import { updateAddress, ApiError } from "@/api/member";
import type { AddressDto, MemberDetailRes } from "@/types/member";
import type { DaumPostcodeData } from "@/types/daum-postcode";
import Header from "@/components/Header";

export default function MyPage() {
  // 주소 관련 상태
  const [postcode, setPostcode] = useState("");
  const [roadAddress, setRoadAddress] = useState("");
  const [sigunguCode, setSigunguCode] = useState("");
  const [bCode, setBCode] = useState("");
  const [addressName, setAddressName] = useState("");

  // UI 상태
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successData, setSuccessData] = useState<MemberDetailRes | null>(null);
  const [isScriptLoaded, setIsScriptLoaded] = useState(false);

  // 카카오 우편번호 검색 열기
  const openPostcodeSearch = () => {
    if (!isScriptLoaded) {
      setError("우편번호 검색 스크립트를 로딩 중입니다.");
      return;
    }

    new window.daum.Postcode({
      oncomplete: (data: DaumPostcodeData) => {
        setPostcode(data.zonecode);
        setRoadAddress(data.roadAddress);
        setSigunguCode(data.sigunguCode);
        setBCode(data.bcode);
        setAddressName(data.address);
        setError(null);
      },
    }).open();
  };

  // 주소 업데이트 제출
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!roadAddress) {
      setError("주소를 먼저 검색해주세요.");
      return;
    }

    setError(null);
    setSuccessData(null);
    setIsLoading(true);

    const addressDto: AddressDto = {
      postcode,
      addressName,
      sigunguCode,
      bCode,
      roadAddress,
      hCode: null, // 백엔드에서 채움
      latitude: null, // 백엔드에서 채움
      longitude: null, // 백엔드에서 채움
    };

    try {
      const response = await updateAddress(addressDto);
      setSuccessData(response);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(`[${err.resultCode}] ${err.msg}`);
      } else {
        setError("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <Script
        src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
        onLoad={() => setIsScriptLoaded(true)}
      />
      <Header />
      <main style={{ padding: "20px" }}>
        <h1>내 정보 수정</h1>

        {error && <div style={{ color: "red", marginBottom: "10px" }}>{error}</div>}

        {successData && (
          <div style={{ color: "green", marginBottom: "10px" }}>
            주소가 성공적으로 업데이트되었습니다.
            <br />
            - 도로명 주소: {successData.roadAddress}
            <br />
            - 행정동 코드: {successData.hCode}
            <br />
            - 위도: {successData.latitude}
            <br />
            - 경도: {successData.longitude}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <h2>주소 정보</h2>

          <div style={{ marginBottom: "10px" }}>
            <button type="button" onClick={openPostcodeSearch}>
              주소 검색
            </button>
          </div>

          <div style={{ marginBottom: "10px" }}>
            <label>우편번호</label>
            <input type="text" value={postcode} readOnly placeholder="주소 검색을 클릭하세요" />
          </div>

          <div style={{ marginBottom: "10px" }}>
            <label>도로명 주소</label>
            <input type="text" value={roadAddress} readOnly placeholder="주소 검색을 클릭하세요" />
          </div>

          <div style={{ marginBottom: "10px" }}>
            <label>시군구 코드</label>
            <input type="text" value={sigunguCode} readOnly />
          </div>

          <div style={{ marginBottom: "10px" }}>
            <label>법정동 코드</label>
            <input type="text" value={bCode} readOnly />
          </div>

          <button type="submit" disabled={isLoading || !roadAddress}>
            {isLoading ? "수정 중..." : "수정하기"}
          </button>
        </form>
      </main>
    </div>
  );
}
