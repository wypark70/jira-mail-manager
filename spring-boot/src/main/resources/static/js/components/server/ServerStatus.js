// js/components/server/ServerStatus.js
const SMTP_API = '/api/smtp';
export class ServerStatus extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({mode: 'open'});
        this.render();
    }

    connectedCallback() {
        this.setupEventListeners();
        // 페이지 로딩 시 서버 상태 로드
        this.loadServerStatus();
    }

    render() {
        this.shadowRoot.innerHTML = `
        <style>
            .status-box {
                padding: 15px;
                margin-bottom: 20px;
                border-radius: 4px;
            }
            .running {
                background-color: #d4edda;
                color: #155724;
                border: 1px solid #c3e6cb;
            }
            .stopped {
                background-color: #f8d7da;
                color: #721c24;
                border: 1px solid #f5c6cb;
            }
            .button-container {
                display: flex;
                gap: 10px;
                flex-direction: row;
                flex-wrap: nowrap;
                justify-content: flex-end;
                align-items: center;
            }
            .button {
                padding: 10px 20px;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-size: 16px;
            }
            .start-button {
                background-color: #28a745;
                color: white;
            }
            .stop-button {
                background-color: #dc3545;
                color: white;
            }
            .disabled {
                opacity: 0.5;
                cursor: not-allowed;
            }
        </style>
        
        <div class="status-box">
            <h2>서버 상태: <span id="statusText">상태</span></h2>
        </div>

        <div class="button-container">
            <button 
                id="startServerBtn" 
                class="button start-button">
                시작
            </button>
            
            <button 
                id="stopServerBtn" 
                class="button stop-button">
                중지
            </button>
        </div>
        `;
    }

    setupEventListeners() {
        this.shadowRoot.getElementById('startServerBtn').addEventListener('click', () => {
            this.startServer();
        });

        this.shadowRoot.getElementById('stopServerBtn').addEventListener('click', () => {
            this.stopServer();
        });
    }

    startServer() {
        fetch(`${SMTP_API}/start`, {
            method: 'POST'
        })
            .then(response => response.text())
            .then(message => {
                alert(message);
                this.loadServerStatus();
            })
            .catch(error => {
                alert('오류가 발생했습니다: ' + error);
            });
    }

    stopServer() {
        fetch(`${SMTP_API}/stop`, {
            method: 'POST'
        })
            .then(response => response.text())
            .then(message => {
                alert(message);
                this.loadServerStatus();
            })
            .catch(error => {
                alert('오류가 발생했습니다: ' + error);
            });
    }

    loadServerStatus() {
        fetch(`${SMTP_API}/status`)
            .then(response => response.json())
            .then(data => {
                const statusBox = this.shadowRoot.querySelector('.status-box');
                const statusText = this.shadowRoot.getElementById('statusText');
                const startButton = this.shadowRoot.getElementById('startServerBtn');
                const stopButton = this.shadowRoot.getElementById('stopServerBtn');

                if (data.running) {
                    statusBox.classList.add('running');
                    statusBox.classList.remove('stopped');
                    statusText.textContent = data.status;
                    startButton.classList.add('disabled');
                    startButton.disabled = true;
                    stopButton.classList.remove('disabled');
                    stopButton.disabled = false;
                } else {
                    statusBox.classList.add('stopped');
                    statusBox.classList.remove('running');
                    statusText.textContent = data.status;
                    startButton.classList.remove('disabled');
                    startButton.disabled = false;
                    stopButton.classList.add('disabled');
                    stopButton.disabled = true;
                }
            })
            .catch(error => {
                console.error('서버 상태를 불러오는 중 오류 발생:', error);
            });
    }
}

// 웹 컴포넌트 등록
customElements.define('server-status', ServerStatus);