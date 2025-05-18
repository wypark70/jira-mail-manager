// EmailView.js - 이메일 상세보기 컴포넌트

export class EmailView extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({mode: 'open'});
        this.render();
    }

    connectedCallback() {
        this.setupEventListeners();

        // 이벤트 구독
        document.addEventListener('show-email-details', (e) => {
            this.loadEmailDetails(e.detail.emailUrl);
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
        .email-content {
          margin-top: 5px;
          padding: 10px;
          border: 1px solid #ddd;
          border-radius: 4px;
          background-color: #f9f9f9;
        }
        .email-body {
          white-space: pre-wrap;
          word-break: break-all;
        }
      </style>
      
      <!-- 이메일 상세보기 모달 -->
      <div id="viewEmailModal" class="modal">
        <div class="modal-content">
          <span class="close" id="closeModalBtn">&times;</span>
          <h2>이메일 상세 정보</h2>
          <div id="emailDetails">
            <div class="form-group">
              <label>발신자</label>
              <div id="viewSender" class="email-content"></div>
            </div>
            <div class="form-group">
              <label>수신자</label>
              <div id="viewRecipient" class="email-content"></div>
            </div>
            <div class="form-group">
              <label>제목</label>
              <div id="viewSubject" class="email-content"></div>
            </div>
            <div class="form-group">
              <label>내용</label>
              <div id="viewBody" class="email-content email-body"></div>
            </div>
          </div>
        </div>
      </div>
    `;
    }

    setupEventListeners() {
        this.shadowRoot.getElementById('closeModalBtn').addEventListener('click', () => {
            this.hideModal();
        });

        // 모달 외부 클릭 시 닫기
        this.shadowRoot.getElementById('viewEmailModal').addEventListener('click', (e) => {
            if (e.target === this.shadowRoot.getElementById('viewEmailModal')) {
                this.hideModal();
            }
        });
    }

    loadEmailDetails(emailUrl) {
        fetch(emailUrl)
            .then(response => response.json())
            .then(email => {
                this.shadowRoot.getElementById('viewSender').textContent = email.sender;
                this.shadowRoot.getElementById('viewRecipient').textContent = email.recipient;
                this.shadowRoot.getElementById('viewSubject').textContent = email.subject;
                this.shadowRoot.getElementById('viewBody').textContent = email.body;
                this.showModal();
            })
            .catch(error => {
                alert('이메일 정보를 불러오는 중 오류가 발생했습니다: ' + error);
            });
    }

    showModal() {
        this.shadowRoot.getElementById('viewEmailModal').style.display = 'block';
    }

    hideModal() {
        this.shadowRoot.getElementById('viewEmailModal').style.display = 'none';
    }
}

customElements.define('email-view', EmailView);
