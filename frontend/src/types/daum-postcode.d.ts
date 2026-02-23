// 카카오 Daum Postcode API 타입 정의

interface DaumPostcodeData {
  zonecode: string; // 우편번호
  address: string; // 기본 주소 (지번 또는 도로명)
  addressType: "R" | "J"; // R: 도로명, J: 지번
  roadAddress: string; // 도로명 주소
  jibunAddress: string; // 지번 주소
  bname: string; // 법정동/법정리 이름
  buildingName: string; // 건물명
  apartment: "Y" | "N"; // 아파트 여부
  sigunguCode: string; // 시군구 코드
  bcode: string; // 법정동 코드
  roadnameCode: string; // 도로명 코드
  sido: string; // 시/도
  sigungu: string; // 시/군/구
}

interface DaumPostcodeOptions {
  oncomplete: (data: DaumPostcodeData) => void;
  onclose?: () => void;
  width?: number | string;
  height?: number | string;
}

interface DaumPostcode {
  new (options: DaumPostcodeOptions): { open: () => void };
}

interface Daum {
  Postcode: DaumPostcode;
}

declare global {
  interface Window {
    daum: Daum;
  }
}

export type { DaumPostcodeData, DaumPostcodeOptions };
