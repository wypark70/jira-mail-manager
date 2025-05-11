<script context="module" lang="ts">
	const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL || 'http://localhost:8080/api/data';

	async function loadMailQueue(): Promise<Mail[]> {
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
				method: 'DELETE'
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

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-2xl font-bold text-gray-800">메일 큐</h1>
		<a
			href="/mail-queue/create"
			class="inline-flex items-center rounded-lg bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
		>
			<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
			</svg>
			새 메일 추가
		</a>
	</div>

	{#if mailQueue.length > 0}
		<div class="overflow-hidden rounded-lg bg-white shadow-md">
			<table class="min-w-full divide-y divide-gray-200">
				<thead class="bg-gray-50">
					<tr>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
							>ID</th
						>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
							>수신자</th
						>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
							>제목</th
						>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
							>생성일</th
						>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
							>발송일</th
						>
						<th
							class="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500"
							>액션</th
						>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 bg-white">
					{#each mailQueue as mail}
						<tr class="hover:bg-gray-50">
							<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{mail.id}</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{mail.recipient}</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{mail.subject}</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
								{new Date(mail.createdAt).toLocaleString()}
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
								{mail.sentAt ? new Date(mail.sentAt).toLocaleString() : '-'}
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
								<a
									href={`/mail-queue/edit/${mail.id}`}
									class="mr-3 text-indigo-600 hover:text-indigo-900">수정</a
								>
								<button on:click={() => deleteMail(mail.id)} class="text-red-600 hover:text-red-900"
									>삭제</button
								>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	{:else}
		<div class="rounded-lg bg-white py-12 text-center shadow-md">
			<svg
				class="mx-auto h-12 w-12 text-gray-400"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
				/>
			</svg>
			<p class="mt-2 text-sm text-gray-500">메일 큐가 비어 있습니다.</p>
		</div>
	{/if}
</div>
