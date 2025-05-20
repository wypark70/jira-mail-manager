<script lang="ts">
    import {DarkMode, Navbar, NavBrand, NavHamburger, NavLi, NavUl} from "flowbite-svelte"
    import logo from "$lib/images/svelte-logo.svg";
    import { page } from '$app/state';


    let currentPath = $state(page.url.pathname);

    const baseClass = 'px-3 py-2 rounded-lg transition-all duration-200';
    const activeClass = `${baseClass} bg-orange-100 dark:bg-orange-900 text-orange-700 dark:text-orange-300 font-bold`;
    const inactiveClass = `${baseClass} text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800`;

    const menuItems = $state([
        {href: '/', label: 'Dashboard', classNm: activeClass},
        {href: '/mail-queue', label: 'Mail Queue', classNm: inactiveClass},
        {href: '/mail-history', label: 'Mail history', classNm: inactiveClass},
        {href: '/log-viewer', label: 'Log viewer', classNm: inactiveClass}
    ]);

    $effect(() => {
        currentPath = page.url.pathname;
        menuItems.forEach(item => {
            const classNm = currentPath === item.href
                ? item.classNm = activeClass
                : item.classNm = inactiveClass
            return {
                name: item.href,
                label: item.label,
                classNm: classNm
            }
        });
    });
</script>

<Navbar>
    <NavBrand href="/">
        <img alt="Jira Mail Manager" class="me-3 h-6 sm:h-9" src={logo}/>
        <span class="self-center text-xl font-semibold whitespace-nowrap dark:text-white">Jira Mail Manager</span>
    </NavBrand>
    <NavHamburger/>
    <NavUl>
        {#each menuItems as {href, label, classNm}}
            <NavLi {href}>
                <div class={classNm}>{label}</div>
            </NavLi>
        {/each}
        <DarkMode/>
    </NavUl>
</Navbar>
