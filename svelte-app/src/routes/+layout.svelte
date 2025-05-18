<script lang="ts">
    import '../app.css';
    import HeaderNav from './HeaderNav.svelte';

    let {children} = $props();
</script>


<main class="mx-auto bg-gray-50 dark:bg-gray-900">
    <HeaderNav/>
    {@render children()}
</main>
