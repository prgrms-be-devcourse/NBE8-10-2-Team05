const STORAGE_KEY = "auth_user";

export function isAuthRequired(url: string): boolean {
    // 1. API 요청이 아닌 경우(일반 페이지 이동 등)는 무조건 인증 체크 통과
    const isApiRequest = url.startsWith("/api") || url.includes(window.location.origin + "/api");
    if (!isApiRequest) {
        return false;
    }

    // 2. API 중에서도 로그인과 회원가입은 인증 없이 호출 가능해야 함
    const EXCLUDED_API_PATHS = [
        "/api/v1/member/member/login",
        "/api/v1/member/member/join",
        "/api/v1/member/member/detail", //초기인증 확인용 api
        "/api/v1/welfare/estate/regions" //행복주택 selectBox세팅용 api
    ];

    const isExcluded = EXCLUDED_API_PATHS.some(path => url.includes(path));
    if (isExcluded) {
        return false;
    }

    // 3. 그 외 모든 /api 요청은 로컬스토리지에 유저 정보가 있어야 함
    const hasAuthInfo = typeof window !== "undefined" && !!localStorage.getItem(STORAGE_KEY);

    // 정보가 없으면 인증이 필요한 상태(true) 반환
    return !hasAuthInfo;
}

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
    if (isAuthRequired(url)) {
        console.warn("[fetchWithAuth] 인증이 필요한 API입니다. 로그인으로 이동합니다.");
        if (typeof window !== "undefined") {
            window.location.href = "/login";
        }
        return Promise.reject("Redirecting to login...");
    }

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
            //cleanup auth비우기
            if (typeof window !== "undefined") {
                localStorage.removeItem(STORAGE_KEY);
                window.location.href = "/login";
            }
            return Promise.reject(reissueErr);
        }
    }

    return response;
}
