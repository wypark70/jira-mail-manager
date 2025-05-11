<script lang="ts">
  import { Dropdown, DropdownItem } from 'flowbite-svelte';
  import { theme, setTheme } from '$lib/stores/theme';
  import { Sun, Moon, Monitor } from 'lucide-svelte';
  import type { Theme } from '$lib/stores/theme';  // Theme 타입 import 추가

  const themes: Array<{
    value: Theme;
    label: string;
    icon: any;  // lucide-svelte의 아이콘 타입을 더 구체적으로 지정할 수 있습니다
  }> = [
    { value: 'light', label: '라이트', icon: Sun },
    { value: 'dark', label: '다크', icon: Moon },
    { value: 'system', label: '시스템', icon: Monitor }
  ];
</script>

<Dropdown>
  <span slot="trigger" class="flex items-center gap-2">
    {#if $theme === 'dark'}
      <Moon class="h-5 w-5" />
    {:else if $theme === 'light'}
      <Sun class="h-5 w-5" />
    {:else}
      <Monitor class="h-5 w-5" />
    {/if}
    <span>테마</span>
  </span>
  
  {#each themes as { value, label, icon: Icon }}
    <DropdownItem 
      on:click={() => setTheme(value)}
    >
      <div class="flex items-center gap-2">
        <Icon class="h-4 w-4" />
        <span>{label}</span>
      </div>
    </DropdownItem>
  {/each}
</Dropdown>