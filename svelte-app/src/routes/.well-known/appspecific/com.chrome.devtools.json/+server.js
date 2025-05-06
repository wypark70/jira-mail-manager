export function GET() {
    return new Response(JSON.stringify({
        // Chrome DevTools에서 필요한 기본 정보
        version: '1.0',
        enabled: true
    }), {
        headers: {
            'Content-Type': 'application/json'
        }
    });
}