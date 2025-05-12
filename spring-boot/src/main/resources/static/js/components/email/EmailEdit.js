// EmailEdit.js - 이메일 수정 컴포넌트
import {EmailUtils} from './EmailUtils.js';

export class EmailEdit extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({mode: 'open'});
        this.currentEmailUrl = '';
        this.render();
    }

    connectedCallback() {
        this.setupEventListeners();

        // 이벤트 구독
        document.addEventListener('show-email-edit-form', (e) => {
            this.loadEmailForEdit(e.detail.emailUrl);
        });
    }

    render() {
        this.shadowRoot.innerHTML = `
      <style>
        .modal {
          display: none;
          position: fixed;
          z-index: 1;
          left: 0;
          top: 0;
          width: 100%;
          height: 100%;
          overflow: auto;
          background-color: rgba(0,0,0,0.4);
        }
        .modal-content {
          background-color: #fefefe;
          margin: 10% auto;
          padding: 20px;
          border: 1px solid #888;
          width: 60%;
          border-radius: 5px;
        }
        .close {
          color: #aaa;
          float: right;
          font-size: 28px;
          font-weight: bold;
          cursor: pointer;
        }
        .close:hover {
          color: black;
        }
        .form-group {
          margin-bottom: 15px;
        }
        .form-group label {
          display: block;
          margin-bottom: 5px;
          font-weight: bold;
        }
        .form-group input, .form-group textarea {
          width: 100%;
          padding: 8px;
          border: 1px solid #ddd;
          border-radius: 4px;
          box-sizing: border-box;
        }
        .form-group textarea {
          height: 100px;
          resize: vertical;
        }
        .submit-button {
          background-color: #4CAF50;
          color: white;
          padding: 10px 20px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 16px;
        }
      </style>
      
      <!-- 이메일 수정 모달 -->
      <div id="editEmailModal" class="modal">
        <div class="modal-content">
          <span class="close" id="closeModalBtn">&times;</span>
          <h2>이메일 수정</h2>
          <form id="editEmailForm">
            <div class="form-group">
              <label for="editSender">발신자</label>
              <input type="email" id="editSender" name="sender" required>
            </div>
            <div class="form-group">
              <label for="editRecipient">수신자</label>
              <input type="email" id="editRecipient" name="recipient" required>
            </div>
            <div class="form-group">
              <label for="editSubject">제목</label>
              <input type="text" id="editSubject" name="subject" required>
            </div>
            <div class="form-group">
              <label for="editBody">내용</label>
              <textarea id="editBody" name="body" required></textarea>
            </div>
            <button type="button" id="submitBtn" class="submit-button">수정</button>
          </form>
        </div>
      </div>
    `;
    }

    setupEventListeners() {
        this.shadowRoot.getElementById('closeModalBtn').addEventListener('click', () => {
            this.hideModal();
        });

        this.shadowRoot.getElementById('submitBtn').addEventListener('click', () => {
            this.updateEmail();
        });

        // 모달 외부 클릭 시 닫기
        this.shadowRoot.getElementById('editEmailModal').addEventListener('click', (e) => {
            if (e.target === this.shadowRoot.getElementById('editEmailModal')) {
                this.hideModal();
            }
        });
    }

    loadEmailForEdit(emailUrl) {
        this.currentEmailUrl = emailUrl;

        fetch(emailUrl)
            .then(response => response.json())
            .then(email => {
                this.shadowRoot.getElementById('editSender').value = email.sender;
                this.shadowRoot.getElementById('editRecipient').value = email.recipient;
                this.shadowRoot.getElementById('editSubject').value = email.subject;
                this.shadowRoot.getElementById('editBody').value = email.body;
                this.showModal();
            })
            .catch(error => {
                alert('이메일 정보를 불러오는 중 오류가 발생했습니다: ' + error);
            });
    }

    showModal() {
        this.shadowRoot.getElementById('editEmailModal').style.display = 'block';
    }

    hideModal() {
        this.shadowRoot.getElementById('editEmailModal').style.display = 'none';
    }

    updateEmail() {
        const formData = new FormData(this.shadowRoot.getElementById('editEmailForm'));
        const email = {
            sender: formData.get('sender'),
            recipient: formData.get('recipient'),
            subject: formData.get('subject'),
            body: formData.get('body')
        };

        fetch(this.currentEmailUrl, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(email)
        })
            .then(response => {
                if (response.ok) {
                    alert('이메일이 성공적으로 수정되었습니다.');
                    this.hideModal();
                    // 이메일 수정 이벤트 발생
                    EmailUtils.dispatchEmailEvent('email-updated');
                } else {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
            })
            .catch(error => {
                alert('이메일 수정 중 오류가 발생했습니다: ' + error);
            });
    }
}

customElements.define('email-edit', EmailEdit);
