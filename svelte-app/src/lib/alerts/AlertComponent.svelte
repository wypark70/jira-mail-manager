<script lang="ts">
    import {fade, fly} from 'svelte/transition';
    import {Alert} from 'flowbite-svelte';
    import {alerts} from './AlertStore';

    function getAlertColor(type: string) {
        switch (type) {
            case 'success':
                return 'green';
            case 'error':
                return 'red';
            case 'warning':
                return 'yellow';
            case 'info':
                return 'blue';
        }
    }
</script>

<div class="fixed top-4 right-4 z-50 flex flex-col gap-2">
    {#each $alerts as alert (alert.id)}
        <div
                in:fly={{ x: 100, duration: 300 }}
                out:fade={{ duration: 300 }}
        >
            <Alert
                    color={getAlertColor(alert.type)}
                    dismissable
            >
                {alert.message}
            </Alert>
        </div>
    {/each}
</div>