import { writable, derived } from 'svelte/store';

export type Theme = 'light' | 'dark' | 'system';

// DOM에 테마 적용
const applyTheme = (theme: 'light' | 'dark') => {
    if (typeof document === 'undefined') return;
    
    const root = document.documentElement;
    root.classList.remove('light', 'dark');
    root.classList.add(theme);
};

// 초기 테마 설정
const initializeTheme = () => {
    const storedTheme = getStoredTheme();
    const initialTheme = storedTheme === 'system' ? getSystemTheme() : storedTheme;
    applyTheme(initialTheme);
    return storedTheme;
};

// 로컬 스토리지에서 테마 설정 불러오기
const getStoredTheme = (): Theme => {
    if (typeof window === 'undefined') return 'system';
    return (localStorage.getItem('theme') as Theme) || 'system';
};

// 시스템 다크모드 감지
const getSystemTheme = (): 'light' | 'dark' => {
    if (typeof window === 'undefined') return 'light';
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

// 테마 스토어 생성
export const theme = writable<Theme>(initializeTheme());

// 현재 적용된 테마 계산과 DOM 적용
export const currentTheme = derived(theme, ($theme) => {
    const resolvedTheme = $theme === 'system' ? getSystemTheme() : $theme;
    applyTheme(resolvedTheme);
    return resolvedTheme;
});

// 테마 변경 함수
export const setTheme = (newTheme: Theme) => {
    theme.set(newTheme);
    localStorage.setItem('theme', newTheme);
};

// 시스템 테마 변경 감지 (브라우저 환경에서만 실행)
if (typeof window !== 'undefined') {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
        const currentStoredTheme = getStoredTheme();
        if (currentStoredTheme === 'system') {
            const newSystemTheme = getSystemTheme();
            applyTheme(newSystemTheme);
        }
    });
}