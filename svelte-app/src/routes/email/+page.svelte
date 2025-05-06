<script>
    import { onMount } from 'svelte';

    // Web Components 로드
    onMount(async () => {
        // 동적으로 Web Component 모듈 import
        await import('$lib/web-components/email/EmailCrudManager.js');
    });
</script>

<svelte:head>
    <title>Email</title>
    <meta name="description" content="List of Emails" />
</svelte:head>

<div class="email-container">
    <!-- Web Components 사용 -->
    <email-crud-manager></email-crud-manager>
</div>

<style>
    .email-container {
        max-width: 1200px;
    }
</style>