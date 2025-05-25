<script>
    import { page } from '$app/state';
</script>

<div class="error-container dark:text-gray-300">
    {#if page.status === 404}
        <h1>404 - 페이지를 찾을 수 없습니다.</h1>
        <p>죄송합니다. 요청하신 페이지를 찾을 수 없습니다.</p>
        <a href="/">홈으로 돌아가기</a>
    {:else}
        <h1>오류 {page.status}</h1>
        <p>{page.error?.message || '알 수 없는 오류가 발생했습니다.'}</p>
    {/if}
</div>

<style>
    .error-container {
        margin-top: -100px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100vh;
        text-align: center;
    }
    h1 {
        font-size: 3em;
        margin-bottom: 20px;
    }
    p {
        font-size: 1.2em;
        margin-bottom: 30px;
    }
    a {
        background-color: #007bff;
        color: white;
        padding: 10px 20px;
        border-radius: 5px;
        text-decoration: none;
    }
    a:hover {
        background-color: #0056b3;
    }
</style>