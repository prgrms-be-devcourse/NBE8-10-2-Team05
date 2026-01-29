"use client";

import Header from "@/components/Header";
import PolicySearch from "@/components/PolicySearch";

export default function HomePage() {
  return (
    <div>
      <Header />
      <main style={{ padding: "20px" }}>
        <h1>정책 검색</h1>
        <PolicySearch />
      </main>
    </div>
  );
}
