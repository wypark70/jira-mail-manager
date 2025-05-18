<script lang="ts">
    import '../app.css';
    import HeaderNav from './HeaderNav.svelte';
    import { AlertComponent } from '$lib/alerts';

    let {children} = $props();
</script>


<main class="mx-auto bg-gray-50 dark:bg-gray-900">
    <AlertComponent />
    <HeaderNav/>
    {@render children()}
</main>
