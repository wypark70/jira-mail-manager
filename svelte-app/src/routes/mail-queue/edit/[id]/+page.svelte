<script context="module">
  const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL || 'http://localhost:8080/api';

  export async function load({ params }) {
    const { id } = params;
    const response = await fetch(`<span class="math-inline">\{springApiBaseUrl\}/mails/</span>{id}`);
    const mail = await response.json();
    return { props: { mail } };
  }
</script>

<script>
  import { goto } from '$app/navigation';

  export let mail;

  let recipient = mail.recipient;
  let subject = mail.subject;
  let body = mail.body;
  let sent_at = mail.sentAt ? new Date(mail.sentAt).toISOString().slice(0, 19) : '';

  async function handleSubmit() {
    const response = await fetch(`<span class="math-inline">\{springApiBaseUrl\}/mails/</span>{mail.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ recipient, subject, body, sentAt: sent_at }),
    });

    if (response.ok) {
      goto('/mail-queue');
    } else {
      const data = await response.json();
      console.error('메일 수정 실패:', data.error);
      alert('메일 수정에 실패했습니다.');
    }
  }
</script>

<h1>메일 수정</h1>

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
  <div>
    <label for="sent_at">발송일:</label>
    <input type="datetime-local" id="sent_at" bind:value={sent_at}>
  </div>
  <button type="submit">저장</button>
</form>

<a href="/mail-queue">목록으로 돌아가기</a>

<style>
  /* ... (이전과 동일) ... */
</style>