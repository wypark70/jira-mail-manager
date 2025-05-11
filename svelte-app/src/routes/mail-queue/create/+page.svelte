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
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({ recipient, subject, body })
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

<div class="container mx-auto px-4 py-8">
	<div class="mx-auto max-w-2xl rounded-lg bg-white p-6 shadow-md">
		<div class="mb-6 flex items-center justify-between">
			<h1 class="text-2xl font-bold text-gray-800">새 메일 추가</h1>
			<a href="/mail-queue" class="flex items-center text-gray-600 hover:text-gray-800">
				<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M10 19l-7-7m0 0l7-7m-7 7h18"
					/>
				</svg>
				목록으로 돌아가기
			</a>
		</div>

		<form on:submit|preventDefault={handleSubmit} class="space-y-6">
			<div>
				<label for="recipient" class="block text-sm font-medium text-gray-700">수신자:</label>
				<input
					type="email"
					id="recipient"
					class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
					bind:value={recipient}
					required
				/>
			</div>

			<div>
				<label for="subject" class="block text-sm font-medium text-gray-700">제목:</label>
				<input
					type="text"
					id="subject"
					class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
					bind:value={subject}
					required
				/>
			</div>

			<div>
				<label for="body" class="block text-sm font-medium text-gray-700">내용:</label>
				<textarea
					id="body"
					rows="5"
					class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
					bind:value={body}
					required
				></textarea>
			</div>

			<div class="flex justify-end">
				<button type="submit" class="rounded-lg bg-blue-500 px-4 py-2 text-white hover:bg-blue-600">
					저장
				</button>
			</div>
		</form>
	</div>
</div>
