// EmailAdd.js - 이메일 추가 컴포넌트
import {EmailUtils} from './EmailUtils.js';

export class EmailAdd extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({mode: 'open'});
        this.render();
    }

    connectedCallback() {
        this.setupEventListeners();

        // 이벤트 구독
        document.addEventListener('show-email-add-form', () => {
            this.showModal();
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
      
      <!-- 이메일 추가 모달 -->
      <div id="addEmailModal" class="modal">
        <div class="modal-content">
          <span class="close" id="closeModalBtn">&times;</span>
          <h2>이메일 추가</h2>
          <form id="addEmailForm">
            <div class="form-group">
              <label for="sender">발신자</label>
              <input type="email" id="sender" name="sender" required>
            </div>
            <div class="form-group">
              <label for="recipient">수신자</label>
              <input type="email" id="recipient" name="recipient" required>
            </div>
            <div class="form-group">
              <label for="subject">제목</label>
              <input type="text" id="subject" name="subject" required>
            </div>
            <div class="form-group">
              <label for="body">내용</label>
              <textarea id="body" name="body" required></textarea>
            </div>
            <button type="button" id="submitBtn" class="submit-button">추가</button>
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
            this.addEmail();
        });

        // 모달 외부 클릭 시 닫기
        this.shadowRoot.getElementById('addEmailModal').addEventListener('click', (e) => {
            if (e.target === this.shadowRoot.getElementById('addEmailModal')) {
                this.hideModal();
            }
        });
    }

    showModal() {
        this.shadowRoot.getElementById('addEmailModal').style.display = 'block';
    }

    hideModal() {
        this.shadowRoot.getElementById('addEmailModal').style.display = 'none';
        this.shadowRoot.getElementById('addEmailForm').reset();
    }

    addEmail() {
        const formData = new FormData(this.shadowRoot.getElementById('addEmailForm'));
        const email = {
            sender: formData.get('sender'),
            recipient: formData.get('recipient'),
            subject: formData.get('subject'),
            body: formData.get('body')
        };

        fetch(EmailUtils.EMAILS_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(email)
        })
            .then(response => {
                if (response.ok) {
                    alert('이메일이 성공적으로 추가되었습니다.');
                    this.hideModal();
                    // 이메일 추가 이벤트 발생
                    EmailUtils.dispatchEmailEvent('email-added');
                } else {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
            })
            .catch(error => {
                alert('이메일 추가 중 오류가 발생했습니다: ' + error);
            });
    }
}

customElements.define('email-add', EmailAdd);
