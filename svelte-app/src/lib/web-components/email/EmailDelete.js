// EmailDelete.js - 이메일 삭제 컴포넌트
import {EmailUtils} from './EmailUtils.js';

export class EmailDelete extends HTMLElement {
    constructor() {
        super();
        // 이 컴포넌트는 UI가 필요 없으므로 Shadow DOM 생성하지 않음
    }

    connectedCallback() {
        // 이메일 삭제 이벤트 리스너
        document.addEventListener('delete-email', (e) => {
            this.deleteEmail(e.detail.emailUrl);
        });
    }

    deleteEmail(emailUrl) {
        if (confirm('정말로 이 이메일을 삭제하시겠습니까?')) {
            fetch(emailUrl, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        alert('이메일이 성공적으로 삭제되었습니다.');
                        // 이메일 삭제 이벤트 발생
                        EmailUtils.dispatchEmailEvent('email-deleted');
                    } else {
                        return response.text().then(text => {
                            throw new Error(text);
                        });
                    }
                })
                .catch(error => {
                    alert('이메일 삭제 중 오류가 발생했습니다: ' + error);
                });
        }
    }
}

customElements.define('email-delete', EmailDelete);
