import type {
  ApplicationItem,
  AddApplicationResponse,
  DeleteApplicationResponse,
} from "@/types/policy";

// GET /api/v1/member/policy-aply/welfare-applications
// 신청 내역 조회
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function getApplications(): Promise<ApplicationItem[]> {
  const response = await fetch(
    `/api/v1/member/policy-aply/welfare-applications`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(`신청 내역 조회 실패: ${response.status}`);
  }

  const data: ApplicationItem[] = await response.json();
  return data;
}

// POST /api/v1/member/policy-aply/welfare-application/{policyId}
// 정책 신청
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function addApplication(
  policyId: number
): Promise<AddApplicationResponse> {
  const response = await fetch(
    `/api/v1/member/policy-aply/welfare-application/${policyId}`,
    {
      method: "POST",
      credentials: "include",
    }
  );

  const data: AddApplicationResponse = await response.json();

  if (!response.ok) {
    throw new Error(data.message || `신청 실패: ${response.status}`);
  }

  return data;
}

// PUT /api/v1/member/policy-aply/welfare-application/{id}
// 신청 취소 (id = Application.id)
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function deleteApplication(
  applicationId: number
): Promise<DeleteApplicationResponse> {
  const response = await fetch(
    `/api/v1/member/policy-aply/welfare-application/${applicationId}`,
    {
      method: "PUT",
      credentials: "include",
    }
  );

  const data: DeleteApplicationResponse = await response.json();

  if (!response.ok) {
    throw new Error(data.message || `신청 취소 실패: ${response.status}`);
  }

  return data;
}
