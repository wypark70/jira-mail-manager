<script lang="ts">
    import {Input} from 'flowbite-svelte';
    import { CopyCheckIcon, CopyIcon, ListOrdered, Search} from 'lucide-svelte';

    const {json} = $props<{
        json: any;
    }>();

    let showLineNumbers = $state(false);
    let searchText = $state('');
    let copySuccess = $state(false);

    // JSON 문자열 변환
    let formattedJson = $derived(JSON.stringify(json, null, 2));
    let lines = $derived(formattedJson.split('\n'));

    function escapeRegExp(string: string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    // 구문 하이라이트 적용
    function syntaxHighlight(text: string, shouldHighlightSearch: boolean = true) {
        // 먼저 검색어 하이라이트를 위한 텍스트 준비
        let highlighted = text
            .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
                function(match) {
                    let cls = 'text-blue-600 dark:text-blue-400'; // 숫자
                    if (/^"/.test(match)) {
                        if (/:$/.test(match)) {
                            cls = 'text-gray-800 dark:text-gray-200'; // 키
                        } else {
                            cls = 'text-green-600 dark:text-green-400'; // 문자열
                        }
                    } else if (/true|false/.test(match)) {
                        cls = 'text-purple-600 dark:text-purple-400'; // 불리언
                    } else if (/null/.test(match)) {
                        cls = 'text-red-600 dark:text-red-400'; // null
                    }
                    return `<span class="${cls}">${match}</span>`;
                });

        // 검색어 하이라이트 적용 (검색어가 있고 하이라이트가 필요한 경우에만)
        if (shouldHighlightSearch && searchText) {
            const searchRegex = new RegExp(escapeRegExp(searchText), 'gi');
            const matches = text.match(searchRegex);

            if (matches) {
                matches.forEach(match => {
                    const searchRegexWithTags = new RegExp(`(?<!<[^>]*)${escapeRegExp(match)}(?![^<]*>)`, 'gi');
                    highlighted = highlighted.replace(
                        searchRegexWithTags,
                        `<mark class="bg-yellow-200 dark:bg-yellow-800">${match}</mark>`
                    );
                });
            }
        }

        return highlighted;
    }


    // 텍스트 검색을 위한 순수 텍스트 추출
    function stripHtml(html: string) {
        const tmp = document.createElement('div');
        tmp.innerHTML = html;
        return tmp.textContent || tmp.innerText || '';
    }

    // 클립보드 복사
    async function copyToClipboard(event: MouseEvent) {
        event.preventDefault();
        event.stopPropagation();
        try {
            await navigator.clipboard.writeText(formattedJson);
            copySuccess = true;
            setTimeout(() => copySuccess = false, 2000);
        } catch (err) {
            console.error('클립보드 복사 실패:', err);
        }
    }
</script>

<div class="json-viewer bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700">
    <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex flex-wrap justify-between gap-3">
        <div class="flex items-center flex-grow relative max-w-md">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search class="h-4 w-4 text-gray-500"/>
            </div>
            <Input
                    bind:value={searchText}
                    class="pl-10"
                    placeholder="JSON 검색..."
                    size="sm"
                    type="text"
            />
        </div>
        <div class="flex items-center gap-2">
            {#if showLineNumbers}
                <ListOrdered onclick={() => showLineNumbers = !showLineNumbers} class="mr-2 h-5 w-5 text-green-500"/>
            {:else}
                <ListOrdered onclick={() => showLineNumbers = !showLineNumbers} class="mr-2 h-5 w-5"/>
            {/if}
            {#if copySuccess}
                <CopyCheckIcon class="mr-2 h-5 w-5 text-green-500"/>
            {:else}
                <CopyIcon onclick={copyToClipboard} class="mr-2 h-5 w-5"/>
            {/if}
        </div>
    </div>

    <div class="overflow-auto p-4 font-mono text-sm relative max-h-[600px]">
        <pre class="whitespace-pre leading-6"><code>{#if showLineNumbers}<div class="table w-full">{#each lines as line, i}<div class="table-row hover:bg-gray-50 dark:hover:bg-gray-900"><div class="table-cell text-gray-400 dark:text-gray-600 w-10 pr-4 text-right select-none font-normal">{i + 1}</div><div class="table-cell">{@html syntaxHighlight(line)}</div></div>{/each}</div>{:else}{@html syntaxHighlight(formattedJson)}{/if}</code></pre>
    </div>
</div>

<style>
    pre {
        margin: 0;
    }

    code {
        color: inherit;
    }

    .table-cell {
        padding: 0 0.25rem;
    }

    .overflow-auto::-webkit-scrollbar {
        width: 8px;
        height: 8px;
    }

    .overflow-auto::-webkit-scrollbar-track {
        background: transparent;
    }

    .overflow-auto::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 4px;
    }
</style>