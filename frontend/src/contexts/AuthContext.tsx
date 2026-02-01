"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";
import { getMemberDetail } from "@/api/member";

// 저장할 사용자 정보
interface User {
  name: string;
  memberId?: number;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  loginUser: (user: User) => void;
  logoutUser: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const STORAGE_KEY = "auth_user";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);


  // TODO: 매번 실행이 아니라 User 없을 떄
  //       그리고 accessToken 재발급 로직이 빠진 것 같다.
  useEffect(() => {
    let cancelled = false;

    const initAuth = async () => {
      try {
        // 1) localStorage 기반 복원
        const stored =
          typeof window !== "undefined"
            ? localStorage.getItem(STORAGE_KEY)
            : null;

        if (stored) {
          try {
            const parsed: User = JSON.parse(stored);
            if (!cancelled) {
              setUser(parsed);
            }
            // 로컬에서 복원되면 서버 조회는 생략
              //서버조회를 통해 데이터 동기화. 대신 로그인 시 한번만 실행
              //return;
            return;
          } catch {
            if (!cancelled && typeof window !== "undefined") {
              localStorage.removeItem(STORAGE_KEY);
            }
          }
        }

        // 2) 서버 세션 기반 복원 (소셜 로그인 포함)
        try {
          const detail = await getMemberDetail();
          if (!cancelled) {
            const serverUser: User = {
              name: detail.name,
              // memberId는 MemberDetailRes 계약에 없으므로 설정하지 않음
            };
            setUser(serverUser);
            if (typeof window !== "undefined") {
              localStorage.setItem(STORAGE_KEY, JSON.stringify(serverUser));
            }
          }
        } catch {
          if (!cancelled) {
            setUser(null);
            // 유령 데이터 삭제
            localStorage.removeItem(STORAGE_KEY);
          }
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    };

    void initAuth();

    return () => {
      cancelled = true;
    };
  }, []);

  // 로그인 시 호출
  const loginUser = (userData: User) => {
    setUser(userData);
    if (typeof window !== "undefined") {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(userData));
    }
  };

  // 로그아웃 시 호출
  const logoutUser = () => {
    setUser(null);
    if (typeof window !== "undefined") {
      localStorage.removeItem(STORAGE_KEY);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, loginUser, logoutUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
