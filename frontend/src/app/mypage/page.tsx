"use client";

import { useState, useEffect } from "react";
import Script from "next/script";
import {
  getMemberDetail,
  updateMemberDetail,
  updateAddress,
  ApiError,
} from "@/api/member";
import type {
  MemberDetailRes,
  MemberDetailReq,
  AddressDto,
} from "@/types/member";
import {
  MarriageStatusLabel,
  EmploymentStatusLabel,
  EducationLevelLabel,
  SpecialStatusLabel,
} from "@/types/member";
import type { DaumPostcodeData } from "@/types/daum-postcode";

export default function MyPage() {
  // 서버에서 불러온 기존 정보
  const [detail, setDetail] = useState<MemberDetailRes | null>(null);
  const [isLoadingDetail, setIsLoadingDetail] = useState(true);

  // 기본 정보 수정 폼 상태
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");

  // 회원 상세 수정 폼 상태
  const [marriageStatus, setMarriageStatus] = useState("");
  const [income, setIncome] = useState("");
  const [employmentStatus, setEmploymentStatus] = useState("");
  const [educationLevel, setEducationLevel] = useState("");
  const [specialStatus, setSpecialStatus] = useState("");

  // 주소 관련 상태
  const [postcode, setPostcode] = useState("");
  const [roadAddress, setRoadAddress] = useState("");
  const [sigunguCode, setSigunguCode] = useState("");
  const [bCode, setBCode] = useState("");
  const [addressName, setAddressName] = useState("");

  // UI 상태
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [isScriptLoaded, setIsScriptLoaded] = useState(false);

  // 페이지 진입 시 회원 상세 정보 불러오기
  useEffect(() => {
    getMemberDetail()
      .then((data) => {
        setDetail(data);
      })
      .catch((err) => {
        if (err instanceof ApiError) {
          setError(`회원 정보 조회 실패: [${err.resultCode}] ${err.msg}`);
        } else {
          setError("회원 정보를 불러올 수 없습니다. 로그인이 필요합니다.");
        }
      })
      .finally(() => setIsLoadingDetail(false));
  }, []);

  // 기본 정보 수정 제출
  const handleBasicSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);
    setIsSubmitting(true);

    const req: MemberDetailReq = {
      name: name || detail?.name || null,
      email: email || detail?.email || null,
    };

    try {
      const updated = await updateMemberDetail(req);
      setDetail(updated);
      setSuccessMsg("기본 정보가 수정되었습니다.");
      setName("");
      setEmail("");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(`[${err.resultCode}] ${err.msg}`);
      } else {
        setError("기본 정보 수정 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // 현재 값의 한글 라벨 가져오기
  const currentMarriageLabel = detail?.marriageStatus
    ? MarriageStatusLabel[detail.marriageStatus] ?? detail.marriageStatus
    : "선택하세요";
  const currentEmploymentLabel = detail?.employmentStatus
    ? EmploymentStatusLabel[detail.employmentStatus] ?? detail.employmentStatus
    : "선택하세요";
  const currentEducationLabel = detail?.educationLevel
    ? EducationLevelLabel[detail.educationLevel] ?? detail.educationLevel
    : "선택하세요";
  const currentSpecialLabel = detail?.specialStatus
    ? SpecialStatusLabel[detail.specialStatus as keyof typeof SpecialStatusLabel] ?? detail.specialStatus
    : "선택하세요";

  // 회원 상세 정보 수정 제출
  const handleDetailSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);
    setIsSubmitting(true);

    const req: MemberDetailReq = {
      marriageStatus:
        (marriageStatus || detail?.marriageStatus || null) as MemberDetailReq["marriageStatus"],
      income: income ? Number(income) : detail?.income ?? null,
      employmentStatus:
        (employmentStatus || detail?.employmentStatus || null) as MemberDetailReq["employmentStatus"],
      educationLevel:
        (educationLevel || detail?.educationLevel || null) as MemberDetailReq["educationLevel"],
      specialStatus: specialStatus || detail?.specialStatus || null,
    };

    try {
      const updated = await updateMemberDetail(req);
      setDetail(updated);
      setSuccessMsg("회원 정보가 수정되었습니다.");
      setMarriageStatus("");
      setIncome("");
      setEmploymentStatus("");
      setEducationLevel("");
      setSpecialStatus("");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(`[${err.resultCode}] ${err.msg}`);
      } else {
        setError("회원 정보 수정 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

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
  const handleAddressSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!roadAddress) {
      setError("주소를 먼저 검색해주세요.");
      return;
    }

    setError(null);
    setSuccessMsg(null);
    setIsSubmitting(true);

    const addressDto: AddressDto = {
      postcode,
      addressName,
      sigunguCode,
      bCode,
      roadAddress,
      hCode: null,
      latitude: null,
      longitude: null,
    };

    try {
      const updated = await updateAddress(addressDto);
      setDetail(updated);
      setSuccessMsg("주소가 수정되었습니다.");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(`[${err.resultCode}] ${err.msg}`);
      } else {
        setError("주소 수정 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoadingDetail) {
    return (
      <main className="ds-page-mid">
        <div className="ds-empty">회원 정보를 불러오는 중...</div>
      </main>
    );
  }

  return (
    <>
      <Script
        src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
        onLoad={() => setIsScriptLoaded(true)}
      />
      <main className="ds-page-mid">
        <h1 className="ds-title">내 정보 수정</h1>

        {error && <div className="ds-alert-error">{error}</div>}
        {successMsg && <div className="ds-alert-success">{successMsg}</div>}

        {/* 기본 정보 수정 */}
        <form onSubmit={handleBasicSubmit}>
          <div className="ds-section">
            <h2>기본 정보</h2>
            <div className="ds-form-group">
              <label className="ds-label">이름</label>
              <input
                type="text"
                className="ds-input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder={detail?.name ?? ""}
              />
            </div>
            <div className="ds-form-group">
              <label className="ds-label">이메일</label>
              <input
                type="email"
                className="ds-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder={detail?.email ?? ""}
              />
            </div>
            <div className="ds-form-group">
              <label className="ds-label">가입 유형</label>
              <input type="text" className="ds-input" value={detail?.type ?? ""} readOnly />
            </div>
            <button type="submit" disabled={isSubmitting} className="ds-btn ds-btn-primary">
              {isSubmitting ? "수정 중..." : "기본 정보 수정하기"}
            </button>
          </div>
        </form>

        {/* 회원 상세 정보 수정 */}
        <form onSubmit={handleDetailSubmit}>
          <div className="ds-section">
            <h2>상세 정보 수정</h2>

            <div className="ds-form-group">
              <label className="ds-label">결혼 상태</label>
              <select
                className="ds-select"
                value={marriageStatus}
                onChange={(e) => setMarriageStatus(e.target.value)}
              >
                <option value="">{currentMarriageLabel}</option>
                {Object.entries(MarriageStatusLabel).map(([key, label]) => (
                  <option key={key} value={key}>{label}</option>
                ))}
              </select>
            </div>

            <div className="ds-form-group">
              <label className="ds-label">소득 (만원)</label>
              <input
                type="number"
                className="ds-input"
                value={income}
                onChange={(e) => setIncome(e.target.value)}
                placeholder={detail?.income != null ? String(detail.income) : "소득을 입력하세요"}
              />
            </div>

            <div className="ds-form-group">
              <label className="ds-label">고용 상태</label>
              <select
                className="ds-select"
                value={employmentStatus}
                onChange={(e) => setEmploymentStatus(e.target.value)}
              >
                <option value="">{currentEmploymentLabel}</option>
                {Object.entries(EmploymentStatusLabel).map(([key, label]) => (
                  <option key={key} value={key}>{label}</option>
                ))}
              </select>
            </div>

            <div className="ds-form-group">
              <label className="ds-label">학력</label>
              <select
                className="ds-select"
                value={educationLevel}
                onChange={(e) => setEducationLevel(e.target.value)}
              >
                <option value="">{currentEducationLabel}</option>
                {Object.entries(EducationLevelLabel).map(([key, label]) => (
                  <option key={key} value={key}>{label}</option>
                ))}
              </select>
            </div>

            <div className="ds-form-group">
              <label className="ds-label">특수 상태</label>
              <select
                className="ds-select"
                value={specialStatus}
                onChange={(e) => setSpecialStatus(e.target.value)}
              >
                <option value="">{currentSpecialLabel}</option>
                {Object.entries(SpecialStatusLabel).map(([key, label]) => (
                  <option key={key} value={key}>{label}</option>
                ))}
              </select>
            </div>

            <button type="submit" disabled={isSubmitting} className="ds-btn ds-btn-primary">
              {isSubmitting ? "수정 중..." : "상세 정보 수정하기"}
            </button>
          </div>
        </form>

        {/* 주소 정보 수정 */}
        <form onSubmit={handleAddressSubmit}>
          <div className="ds-section">
            <h2>주소 정보 수정</h2>

            {detail?.roadAddress && (
              <div className="ds-meta" style={{ marginBottom: "12px" }}>
                현재 주소: {detail.roadAddress}
              </div>
            )}

            <div style={{ marginBottom: "16px" }}>
              <button type="button" onClick={openPostcodeSearch} className="ds-btn ds-btn-secondary">
                주소 검색
              </button>
            </div>

            <div className="ds-form-group">
              <label className="ds-label">우편번호</label>
              <input type="text" className="ds-input" value={postcode} readOnly placeholder="주소 검색을 클릭하세요" />
            </div>

            <div className="ds-form-group">
              <label className="ds-label">도로명 주소</label>
              <input type="text" className="ds-input" value={roadAddress} readOnly placeholder="주소 검색을 클릭하세요" />
            </div>

            <div className="ds-form-group">
              <label className="ds-label">시군구 코드</label>
              <input type="text" className="ds-input" value={sigunguCode} readOnly />
            </div>

            <div className="ds-form-group">
              <label className="ds-label">법정동 코드</label>
              <input type="text" className="ds-input" value={bCode} readOnly />
            </div>

            <button type="submit" disabled={isSubmitting || !roadAddress} className="ds-btn ds-btn-primary">
              {isSubmitting ? "수정 중..." : "주소 수정하기"}
            </button>
          </div>
        </form>
      </main>
    </>
  );
}
