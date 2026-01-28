// Estate(행복주택) 관련 타입 정의

export interface EstateItem {
  pblancId: string;
  pblancNm: string;
  sttusNm: string;
  rcritPblancDe: string;
  beginDe: string;
  endDe: string;
  suplyHoCo: string;
  houseSn: number;
  suplyInsttNm: string;
  houseTyNm: string;
  suplyTyNm: string;
  hsmpNm: string;
  brtcNm: string;
  signguNm: string;
  fullAdres: string;
  rentGtn: number;
  mtRntchrg: number;
  url: string;
}

export interface EstateSearchParams {
  signguCode?: string | null;
  suplyTy?: string | null;
  houseTy?: string | null;
  lfstsTyAt?: string | null;
  bassMtRntchrgSe?: string | null;
  yearMtBegin?: string | null;
  yearMtEnd?: string | null;
}
