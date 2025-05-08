<script context="module" lang="ts">
    import { goto } from '$app/navigation';
  
    const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL || 'http://localhost:8080/api';
  
    let recipient: string = '';
    let subject: string = '';
    let body: string = '';
  
    async function handleSubmit(): Promise<void> {
      const response = await fetch(`${springApiBaseUrl}/mails`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ recipient, subject, body }),
      });
  
      if (response.ok) {
        goto('/mail-queue');
      } else {
        const data = await response.json();
        console.error('메일 생성 실패:', data.error);
        alert('메일 생성에 실패했습니다.');
      }
    }
  </script>
  
  <h1>새 메일 추가</h1>
  
  <form on:submit|preventDefault={handleSubmit}>
    <div>
      <label for="recipient">수신자:</label>
      <input type="email" id="recipient" bind:value={recipient} required>
    </div>
    <div>
      <label for="subject">제목:</label>
      <input type="text" id="subject" bind:value={subject} required>
    </div>
    <div>
      <label for="body">내용:</label>
      <textarea id="body" bind:value={body} rows="5" required></textarea>
    </div>
    <button type="submit">저장</button>
  </form>
  
  <a href="/mail-queue">목록으로 돌아가기</a>
  
  <style>
    /* ... (이전과 동일) ... */
  </style>