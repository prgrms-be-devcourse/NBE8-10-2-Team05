export async function startBatchJob(): Promise<string> {
  const response = await fetch(`/api/v1/batch/start`, {
    method: "POST",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("배치 실행 실패");
  }
  return response.text();
}
