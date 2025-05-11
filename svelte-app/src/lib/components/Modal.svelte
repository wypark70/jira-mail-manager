<script lang="ts">
    export let show = false;
    export let title = '';
    export let message = '';
    export let onClose = () => {};

    function handleKeydown(e: KeyboardEvent) {
        if (e.key === 'Escape') {
            onClose();
        }
    }
</script>

<svelte:window on:keydown={handleKeydown}/>

{#if show}
    <div class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
        <div class="flex min-h-screen items-end justify-center px-4 pb-20 pt-4 text-center sm:block sm:p-0">
            <!-- 배경 오버레이 -->
            <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true" on:click={onClose}></div>

            <!-- 모달 센터링을 위한 트릭 -->
            <span class="hidden sm:inline-block sm:h-screen sm:align-middle" aria-hidden="true">&#8203;</span>

            <div class="inline-block transform overflow-hidden rounded-lg bg-white p-5 text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:align-middle dark:bg-gray-800">
                <div class="mb-4">
                    <h3 class="text-lg font-medium leading-6 text-gray-900 dark:text-gray-100" id="modal-title">
                        {title}
                    </h3>
                </div>
                <div class="mb-4">
                    <p class="text-sm text-gray-500 dark:text-gray-400">
                        {message}
                    </p>
                </div>
                <div class="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                    <button
                        type="button"
                        class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
                        on:click={onClose}
                    >
                        확인
                    </button>
                </div>
            </div>
        </div>
    </div>
{/if}