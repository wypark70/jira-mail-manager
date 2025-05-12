<script lang="ts">
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

{#if show}
    <div class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
        <!-- 배경 오버레이 - opacity 수정 -->
        <div 
            class="fixed inset-0 transition-opacity bg-gray-500/70" 
            aria-hidden="true" 
            on:click={onCancel}
        ></div>

        <div class="flex min-h-screen items-center justify-center p-4 text-center sm:p-0">
            <div class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg dark:bg-gray-800">
                <div class="px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                    <div class="sm:flex sm:items-start">
                        <div class="mt-3 text-center sm:mt-0 sm:text-left w-full">
                            <h3 class="text-lg font-medium leading-6 text-gray-900 dark:text-white" id="modal-title">
                                {title}
                            </h3>
                            <div class="mt-2">
                                <p class="text-sm text-gray-500 dark:text-gray-400">
                                    {message}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                    <button
                        type="button"
                        class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
                        on:click={onConfirm}
                    >
                        확인
                    </button>
                    <button
                        type="button"
                        class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm dark:bg-gray-600 dark:text-gray-200 dark:border-gray-500 dark:hover:bg-gray-500"
                        on:click={onCancel}
                    >
                        취소
                    </button>
                </div>
            </div>
        </div>
    </div>
{/if}