<script lang="ts">
    import {Button, Card, Modal, P } from 'flowbite-svelte';

    export let show = false;
    export let title = '';
    export let message = '';
    export let onConfirm: () => Promise<void>;
    export let onCancel: () => void;

    function handleKeydown(e: KeyboardEvent) {
        if (e.key === 'Escape') {
            onCancel();
        }
    }
</script>

<svelte:window on:keydown={handleKeydown}/>

<Modal
        title={title}
        bind:open={show}
        onclose={onCancel}
        footerClass="flex flex-row-reverse gap-4">
    <p class="text-base leading-relaxed text-gray-500 dark:text-gray-400">{message}</p>

    {#snippet footer()}
        <Button color="alternative" onclick={onCancel}>취소</Button>
        <Button onclick={onConfirm}>확인</Button>
    {/snippet}
</Modal>
