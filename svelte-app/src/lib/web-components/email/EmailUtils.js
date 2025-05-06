// EmailUtils.js - 공통 유틸리티 클래스
export class EmailUtils {
    static API_BASE = '/api/data';
    static EMAILS_API = `${EmailUtils.API_BASE}/emailQueues`;

    // 이메일 ID 추출 (URL에서)
    static getEmailIdFromUrl(url) {
        return url.split('/').pop();
    }

    // 새로운 이메일 이벤트 발생
    static dispatchEmailEvent(eventName, detail = {}) {
        const event = new CustomEvent(eventName, {
            bubbles: true,
            composed: true, // Shadow DOM 경계를 넘어 이벤트 전파 허용
            detail
        });
        document.dispatchEvent(event);
    }
}
