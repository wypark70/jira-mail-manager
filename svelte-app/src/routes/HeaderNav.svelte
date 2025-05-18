<script>
    import {DarkMode, Navbar, NavBrand, NavHamburger, NavLi, NavUl} from "flowbite-svelte";
    import logo from "$lib/images/svelte-logo.svg";
</script>

<header>
    <Navbar>
        <NavBrand href="/">
            <img alt="Jira Mail Manager" class="me-3 h-6 sm:h-9" src={logo}/>
            <span class="self-center text-xl font-semibold whitespace-nowrap dark:text-white">Jira Mail Manager</span>
        </NavBrand>
        <NavHamburger/>
        <NavUl>
            <NavLi href="/">Dashboard</NavLi>
            <NavLi href="/mail-queue">Mail Queue</NavLi>
            <NavLi href="/log-viewer">Log viewer</NavLi>
            <DarkMode/>
        </NavUl>
    </Navbar>
</header>