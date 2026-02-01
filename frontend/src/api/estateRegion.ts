import {EstateRegionResponse, EstateSearchRequest, EstateSearchResponse} from "@/types/estate";

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
 * 주택 지역 select박스 내용물 받아오기
 * GET /api/v1/welfare/estate/regions
 */
export async function searchEstateRegions(
): Promise<EstateRegionResponse> {
    const response = await fetch(
        `/api/v1/welfare/estate/regions`,
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
