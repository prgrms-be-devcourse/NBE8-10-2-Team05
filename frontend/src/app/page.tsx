"use client";

import PolicySearch from "@/components/PolicySearch";

export default function HomePage() {
  return (
    <div>
      <main style={{ padding: "20px" }}>
        <h1>정책 검색</h1>
        <PolicySearch />
      </main>
    </div>
  );
}
