<script context="module" lang="ts">
    const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL || 'http://localhost:8080/api/data';
  
    async function loadMailQueue(): Promise<Mail[]> {
      console.log(">>>>>>>>>>>>>>>>>>>>>>", springApiBaseUrl);
      const response = await fetch(`${springApiBaseUrl}/emailQueues?page=0&size=10&sort=id,desc`);
      const data = await response.json();
      if (response.ok) {
        return data._embedded?.emailQueues as Mail[];
      } else {
        console.error('메일 큐 로딩 실패:', data.error);
        return [];
      }
    }
  
    let mailQueue: Mail[] = await loadMailQueue();
  
    async function deleteMail(id: number): Promise<void> {
      if (confirm('정말로 삭제하시겠습니까?')) {
        const response = await fetch(`${springApiBaseUrl}/emailQueues/{id}`, {
          method: 'DELETE',
        });
        if (response.ok) {
          mailQueue = mailQueue.filter((mail) => mail.id !== id);
        } else {
          const data = await response.json();
          console.error('메일 삭제 실패:', data.error);
          alert('메일 삭제에 실패했습니다.');
        }
      }
    }
  </script>
  
  <h1>메일 큐</h1>
  
  {#if mailQueue.length > 0}
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>수신자</th>
          <th>제목</th>
          <th>생성일</th>
          <th>발송일</th>
          <th>액션</th>
        </tr>
      </thead>
      <tbody>
        {#each mailQueue as mail}
          <tr>
            <td>{mail.id}</td>
            <td>{mail.recipient}</td>
            <td>{mail.subject}</td>
            <td>{new Date(mail.createdAt).toLocaleString()}</td>
            <td>{mail.sentAt ? new Date(mail.sentAt).toLocaleString() : '-'}</td>
            <td>
              <a href={`/mail-queue/edit/${mail.id}`}>수정</a>
              <button on:click={() => deleteMail(mail.id)}>삭제</button>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {:else}
    <p>메일 큐가 비어 있습니다.</p>
  {/if}
  
  <a href="/mail-queue/create">새 메일 추가</a>
  
  <style>
    /* ... (이전과 동일) ... */
  </style>