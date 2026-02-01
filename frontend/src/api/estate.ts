import { EstateSearchRequest, EstateSearchResponse } from "@/types/estate";

export class ApiError extends Error {
  constructor(
    public resultCode: string,
    public msg: string
  ) {
    super(`${resultCode}: ${msg}`);
    this.name = "ApiError";
  }
}

/**
 * 행복주택 검색
 * GET /api/v1/welfare/estate/location
 */
export async function searchEstates(
  req: EstateSearchRequest
): Promise<EstateSearchResponse> {
  const params = new URLSearchParams({
    // sido: req.sido,
    // signguNm: req.signguNm,
      searchKeyword: req.searchKeyword
  });

  const response = await fetch(
    `/api/v1/welfare/estate/location?${params.toString()}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
    }
  );

  if (!response.ok) {
    const errorData = await response.json();
    throw new ApiError(errorData.resultCode || "UNKNOWN", errorData.msg || "Unknown error");
  }

  return response.json();
}
