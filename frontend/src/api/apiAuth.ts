const STORAGE_KEY = "auth_user";

// accessToken 재발급 요청
// POST /api/v1/auth/reissue
async function reissue(): Promise<boolean> {
    const response = await fetch(`/api/v1/auth/reissue`, {
        method: "POST",
        credentials: "include",
    });
    return response.ok;
}

export async function fetchWithAuth(
    url: string,
    options: RequestInit
): Promise<Response> {
    const response = await fetch(url, {
        ...options,
        credentials: "include",
    });

    if (response.status === 401) {
        console.log("[fetchWithAuth] 401 감지, reissue 시도...");
        try {
            const reissued = await reissue();
            console.log("[fetchWithAuth] reissue 결과:", reissued);
            if (reissued) {
                console.log("[fetchWithAuth] 원래 요청 재시도...");
                return await fetch(url, {
                    ...options,
                    credentials: "include",
                });
            }else{
                //TODO: 중복로그인 혹은 refreshToken유효기간 만료 등 혹시나 하는 상황대비
                throw new Error("reissue_failed");
            }
        } catch (reissueErr) {
            console.error("[fetchWithAuth] 인증 만료. 로그인 페이지로 이동합니다 : ", reissueErr);
            if (typeof window !== "undefined") {
                localStorage.removeItem(STORAGE_KEY); // 유령 데이터 삭제
                window.location.href = "/login";      // 강제 이동
            }
            return Promise.reject(reissueErr);
        }
    }

    return response;
}
