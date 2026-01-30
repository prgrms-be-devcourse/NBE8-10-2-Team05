export async function searchPolicies(params: any) {
  // null이나 undefined 값 제거
  const cleanParams: any = {};
  Object.keys(params).forEach(key => {
    if (params[key] !== null && params[key] !== undefined && params[key] !== "") {
      cleanParams[key] = params[key];
    }
  });

  // from, size 기본값 설정
  if (cleanParams.from === undefined) cleanParams.from = 0;
  if (cleanParams.size === undefined) cleanParams.size = 10;

  const query = new URLSearchParams(cleanParams).toString();
  const response = await fetch(`/api/v1/welfare/policy/search?${query}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Elasticsearch 검색 실패");
  }
  return response.json();
}

export async function searchPolicyDb(params: any) {
  // null이나 undefined 값 제거
  const cleanParams: any = {};
  Object.keys(params).forEach(key => {
    if (params[key] !== null && params[key] !== undefined && params[key] !== "") {
      cleanParams[key] = params[key];
    }
  });

  const query = new URLSearchParams(cleanParams).toString();
  const response = await fetch(`/api/v1/welfare/policy/search/db?${query}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("DB 검색 실패");
  }
  return response.json();
}

export async function fetchPolicies(): Promise<string> {
  const response = await fetch(`/api/v1/welfare/policy/list`, {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("데이터 적재 실패");
  }
  return response.text();
}
