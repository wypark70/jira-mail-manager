<script>
	// 필요한 스크립트 작성
	import SmtpServerStatus from '$lib/components/SmtpServerStatus.svelte';
	import LogViewer from '$lib/components/LogViewer.svelte';
	import SmtpLogs from '$lib/components/SmtpLogs.svelte';
</script>

<div class="container mx-auto px-4 py-8">
	<!-- SMTP 서버 상태 -->
	<div class="mb-8">
		<SmtpServerStatus />
	</div>
	<div class="mb-8">
		<SmtpLogs />
	</div>
</div>
