import type { EstateItem, EstateSearchParams } from "@/types/estate";
import type { ApiErrorResponse } from "@/types/member";
import { ApiError } from "@/api/member";

export async function fetchEstateList(
  params: EstateSearchParams
): Promise<EstateItem[]> {
  const query = new URLSearchParams();

  if (params.signguCode) query.set("signguCode", params.signguCode);
  if (params.suplyTy) query.set("suplyTy", params.suplyTy);
  if (params.houseTy) query.set("houseTy", params.houseTy);
  if (params.lfstsTyAt) query.set("lfstsTyAt", params.lfstsTyAt);
  if (params.bassMtRntchrgSe) query.set("bassMtRntchrgSe", params.bassMtRntchrgSe);
  if (params.yearMtBegin) query.set("yearMtBegin", params.yearMtBegin);
  if (params.yearMtEnd) query.set("yearMtEnd", params.yearMtEnd);

  const queryString = query.toString();
  const url = `/api/v1/welfare/estate/list${queryString ? `?${queryString}` : ""}`;

  const response = await fetch(url, {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: EstateItem[] = await response.json();
  return data;
}
