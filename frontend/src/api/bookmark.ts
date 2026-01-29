import type { BookmarkPolicyResponse } from "@/types/policy";

// GET /api/v1/member/bookmark/welfare-bookmarks
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function getBookmarks(): Promise<BookmarkPolicyResponse> {
  const response = await fetch(`/api/v1/member/bookmark/welfare-bookmarks`, {
    method: "GET",
    credentials: "include",
  });

  const data: BookmarkPolicyResponse = await response.json();
  return data;
}
