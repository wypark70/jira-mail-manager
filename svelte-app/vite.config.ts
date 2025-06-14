import tailwindcss from '@tailwindcss/vite';
import {svelteTesting} from '@testing-library/svelte/vite';
import {sveltekit} from '@sveltejs/kit/vite';
import {defineConfig} from 'vite';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {storybookTest} from '@storybook/experimental-addon-test/vitest-plugin';

const dirname =
    typeof __dirname !== 'undefined' ? __dirname : path.dirname(fileURLToPath(import.meta.url));

// More info at: https://storybook.js.org/docs/writing-tests/test-addon
export default defineConfig({
    plugins: [tailwindcss(), sveltekit()],
    server: {
        fs: {
            allow: ['.']
        },
        proxy: {
            '/api': {
                target: 'http://localhost:2500',
                changeOrigin: true, // CORS 문제 해결에 도움이 될 수 있습니다.
                rewrite: (path) => path.replace(/^\/api/, '/api'), // 요청 경로에서 '/api' 제거
                // headers: { 'X-Custom-Header': 'foobar' }, // 사용자 정의 헤더 추가
                // methods: ['GET', 'POST', 'PUT', 'DELETE'], // 허용할 HTTP 메서드
                auth: 'admin:admin'
            },
        },
    },
    build: {
        target: 'esnext' // Change target to esnext to support top-level await
    },
    test: {
        workspace: [
            {
                extends: './vite.config.ts',
                plugins: [svelteTesting()],
                test: {
                    name: 'client',
                    environment: 'jsdom',
                    clearMocks: true,
                    include: ['src/**/*.svelte.{test,spec}.{js,ts}'],
                    exclude: ['src/lib/server/**'],
                    setupFiles: ['./vitest-setup-client.ts']
                }
            },
            {
                extends: './vite.config.ts',
                test: {
                    name: 'server',
                    environment: 'node',
                    include: ['src/**/*.{test,spec}.{js,ts}'],
                    exclude: ['src/**/*.svelte.{test,spec}.{js,ts}']
                }
            },
            {
                extends: true,
                plugins: [
                    // The plugin will run tests for the stories defined in your Storybook config
                    // See options at: https://storybook.js.org/docs/writing-tests/test-addon#storybooktest
                    storybookTest({
                        configDir: path.join(dirname, '.storybook')
                    })
                ],
                test: {
                    name: 'storybook',
                    browser: {
                        enabled: true,
                        headless: true,
                        provider: 'playwright',
                        instances: [
                            {
                                browser: 'chromium'
                            }
                        ]
                    },
                    setupFiles: ['.storybook/vitest.setup.ts']
                }
            }
        ]
    }
});
