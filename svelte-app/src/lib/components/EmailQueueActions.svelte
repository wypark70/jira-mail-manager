<script lang="ts">
    import { Card } from 'flowbite-svelte';
</script>
<div class="grid grid-cols-1 gap-6 md:grid-cols-2 dark:text-white">
    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg"
            href="/mail-queue"
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">메일 큐 관리</h3>
        <p>메일 큐를 확인하고 관리합니다.</p>
    </Card>

    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg"
            href="/mail-queue/create"
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">새 메일 작성</h3>
        <p>새로운 메일을 작성하고 예약합니다.</p>
    </Card>
</div>