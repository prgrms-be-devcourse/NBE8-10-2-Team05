"use client";

import Link from "next/link";
import PolicySearch from "@/components/PolicySearch";

export default function HomePage() {
  return (
    <div>
      <main style={{ padding: "20px" }}>
        <div style={{ marginBottom: "20px", textAlign: "right" }}>
          <Link href="/policy/test">
            <button style={{ padding: "8px 16px", backgroundColor: "#00b894", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
              📊 정책 검색 성능 테스트
            </button>
          </Link>
        </div>
        <h1>정책 검색</h1>
        <PolicySearch />
      </main>
    </div>
  );
}
