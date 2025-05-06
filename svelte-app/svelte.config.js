import adapter from '@sveltejs/adapter-static'; // 정적 어댑터 사용

/** @type {import('@sveltejs/kit').Config} */
const config = {
  kit: {
    adapter: adapter({
      // 정적 파일이 생성될 위치 지정
      pages: 'build',
      assets: 'build',
      fallback: 'index.html', // SPA를 위한 중요 설정 - 모든 경로가 index.html로 리다이렉트
      precompress: false
    }),
    // 필요한 경우 경로 접두사 설정
    paths: {
      base: ''
    },
  },
  compilerOptions: {
    customElement: true
  },
};

export default config;