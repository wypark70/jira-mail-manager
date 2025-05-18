// EmailList.js - 이메일 목록 컴포넌트
import { EmailUtils } from "./EmailUtils.js";

export class EmailList extends HTMLElement {
    constructor() {
        super();

        this.attachShadow({mode: 'open'});
        this.currentPage = 0;
        this.pageSize = 10;
        this.totalPages = 0;
        this.selectedEmails = new Set(); // 선택된 이메일 저장

        this.render();
    }

    connectedCallback() {
        this.loadEmails(this.currentPage, this.pageSize);
        this.setupEventListeners();

        // 다른 컴포넌트에서 발생한 이벤트 구독
        document.addEventListener('email-added', () => this.loadEmails(0, this.pageSize));
        document.addEventListener('email-updated', () => this.loadEmails(this.currentPage, this.pageSize));
        document.addEventListener('email-deleted', () => {
            // 현재 페이지에 항목이 하나만 있고 첫 페이지가 아니라면 이전 페이지로 이동
            const emailsInPage = this.shadowRoot.querySelectorAll('#emailTableBody tr').length;
            if (emailsInPage === 1 && this.currentPage > 0) {
                this.loadEmails(this.currentPage - 1, this.pageSize);
            } else {
                this.loadEmails(this.currentPage, this.pageSize);
            }
        });
    }

    render() {
        this.shadowRoot.innerHTML = `
      <style>
        table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 20px;
        }
        th, td {
          padding: 12px;
          text-align: left;
          border-bottom: 1px solid #ddd;
        }
        th {
          background-color: #f2f2f2;
          font-weight: bold;
        }
        tr:hover {
          background-color: #f5f5f5;
        }
        .button-container {
          display: flex;
          gap: 10px;
          flex-direction: row;
          flex-wrap: nowrap;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;
        }
        .right-buttons {
          display: flex;
          gap: 10px;
          align-items: center;
        }
        .add-button {
          background-color: #007bff;
          color: white;
          padding: 10px 20px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 16px;
        }
        .delete-selected-button {
          background-color: #dc3545;
          color: white;
          padding: 10px 20px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 16px;
          opacity: 0.5;
          pointer-events: none;
        }
        .delete-selected-button.active {
          opacity: 1;
          pointer-events: auto;
        }
        .action-button {
          padding: 5px 10px;
          font-size: 14px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          margin-right: 5px;
        }
        .edit-button {
          background-color: #ffc107;
          color: #212529;
        }
        .no-emails {
          padding: 20px;
          text-align: center;
          color: #666;
          font-style: italic;
        }
        .pagination {
          display: flex;
          justify-content: center;
          margin-top: 20px;
        }
        .pagination ul {
          display: flex;
          padding: 0;
          margin: 0;
          list-style: none;
        }
        .pagination li {
          margin: 0 3px;
        }
        .pagination a {
          display: block;
          text-decoration: none;
          padding: 8px 12px;
          border: 1px solid #ddd;
          color: #007bff;
          border-radius: 4px;
          transition: all 0.3s ease;
        }
        .pagination a:hover {
          background-color: #f0f0f0;
        }
        .pagination .active a {
          background-color: #007bff;
          color: white;
          border-color: #007bff;
          cursor: default;
        }
        .pagination .disabled a {
          color: #6c757d;
          pointer-events: none;
          cursor: not-allowed;
          background-color: #fff;
          border-color: #ddd;
          opacity: 0.65;
        }
        .pagination-list {
          align-items: center;
        }
        .page-size-container {
          display: flex;
          align-items: center;
          gap: 10px;
        }
        .page-size-container label {
          font-weight: bold;
        }
        .page-size-container select {
          padding: 8px;
          border-radius: 4px;
          border: 1px solid #ddd;
        }
        .email-title-link {
          color: #007bff;
          text-decoration: none;
          cursor: pointer;
        }
        .email-title-link:hover {
          text-decoration: underline;
        }
        .checkbox-header {
          width: 40px;
        }
      </style>
      
      <div>
        <h2>이메일 목록</h2>
        
        <div class="button-container">
            <div class="page-size-container">
                <label for="pageSizeSelect">페이지 크기:</label>
                <select id="pageSizeSelect">
                    <option value="5">5</option>
                    <option value="10" selected>10</option>
                    <option value="20">20</option>
                    <option value="50">50</option>
                </select>
            </div>
            <div class="right-buttons">
                <button id="deleteSelectedBtn" class="delete-selected-button">삭제</button>
                <button id="addEmailBtn" class="add-button">추가</button>
            </div>
        </div>
        <div id="no-emails-message" class="no-emails" style="display: none">
          저장된 이메일이 없습니다.
        </div>
        
        <table id="emailTable">
          <thead>
            <tr>
              <th class="checkbox-header">
                <input type="checkbox" id="selectAllCheckbox" title="전체 선택">
              </th>
              <th>번호</th>
              <th>발신자</th>
              <th>수신자</th>
              <th>제목</th>
              <th>동작</th>
            </tr>
          </thead>
          <tbody id="emailTableBody">
            <!-- 이메일 데이터가 JavaScript로 채워집니다 -->
          </tbody>
        </table>
        
        <div id="pagination" class="pagination">
          <!-- 페이징 버튼이 JavaScript로 채워집니다 -->
        </div>
      </div>
    `;
    }

    setupEventListeners() {
        // 이메일 추가 버튼
        this.shadowRoot.getElementById('addEmailBtn').addEventListener('click', () => {
            this.dispatchAddEmailEvent();
        });

        // 페이지 크기 변경 이벤트
        this.shadowRoot.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.pageSize = parseInt(e.target.value);
            this.loadEmails(0, this.pageSize);
        });

        // 선택 삭제 버튼
        this.shadowRoot.getElementById('deleteSelectedBtn').addEventListener('click', () => {
            this.deleteSelectedEmails();
        });

        // 전체 선택 체크박스
        this.shadowRoot.getElementById('selectAllCheckbox').addEventListener('change', (e) => {
            const checkboxes = this.shadowRoot.querySelectorAll('tbody input[type="checkbox"]');
            checkboxes.forEach(checkbox => {
                checkbox.checked = e.target.checked;

                if (e.target.checked) {
                    this.selectedEmails.add(checkbox.dataset.url);
                } else {
                    this.selectedEmails.delete(checkbox.dataset.url);
                }
            });

            this.updateDeleteButtonState();
        });
    }

    dispatchAddEmailEvent() {
        EmailUtils.dispatchEmailEvent('show-email-add-form');
    }

    // 선택 삭제 버튼 상태 업데이트
    updateDeleteButtonState() {
        const deleteBtn = this.shadowRoot.getElementById('deleteSelectedBtn');
        if (this.selectedEmails.size > 0) {
            deleteBtn.classList.add('active');
        } else {
            deleteBtn.classList.remove('active');
        }
    }

    // 전체 선택 체크박스 초기화
    resetSelectAllCheckbox() {
        const selectAllCheckbox = this.shadowRoot.getElementById('selectAllCheckbox');
        if (selectAllCheckbox) {
            selectAllCheckbox.checked = false;
        }
    }

    deleteSelectedEmails() {
        if (this.selectedEmails.size === 0) {
            alert('삭제할 이메일을 선택해주세요.');
            return;
        }

        if (confirm(`선택한 ${this.selectedEmails.size}개의 이메일을 삭제하시겠습니까?`)) {
            // 선택된 이메일 수와 현재 페이지의 이메일 수 비교를 위해 저장
            const currentEmailCount = this.shadowRoot.querySelectorAll('#emailTableBody tr').length;
            const isDeletingAllInPage = this.selectedEmails.size === currentEmailCount;

            const deletePromises = Array.from(this.selectedEmails).map(url =>
                fetch(url, {
                    method: 'DELETE'
                })
            );

            Promise.all(deletePromises)
                .then(() => {
                    this.selectedEmails.clear();
                    this.resetSelectAllCheckbox();
                    this.updateDeleteButtonState();

                    // 마지막 페이지의 모든 데이터가 삭제되었고, 첫 페이지가 아니라면 이전 페이지로 이동
                    if (isDeletingAllInPage && this.currentPage > 0 && this.currentPage === this.totalPages - 1) {
                        this.loadEmails(this.currentPage - 1, this.pageSize);
                    } else {
                        this.loadEmails(this.currentPage, this.pageSize);
                    }

                    alert('선택한 이메일이 삭제되었습니다.');
                })
                .catch(error => {
                    console.error('이메일 삭제 중 오류 발생:', error);
                    alert('이메일 삭제에 실패했습니다.');
                });
        }
    }

    loadEmails(page, size) {
        // 페이지가 범위를 벗어나지 않도록 확인
        page = Math.max(0, page);

        // 페이지 크기 셀렉트 박스 값 설정
        const pageSizeSelect = this.shadowRoot.getElementById('pageSizeSelect');
        if (pageSizeSelect) {
            pageSizeSelect.value = size.toString();
        }

        fetch(`${EmailUtils.EMAILS_API}?page=${page}&size=${size}&sort=id,desc`)
            .then(response => response.json())
            .then(data => {
                const emailTableBody = this.shadowRoot.getElementById('emailTableBody');
                emailTableBody.innerHTML = '';

                // 선택된 이메일 초기화
                this.selectedEmails.clear();
                this.resetSelectAllCheckbox();
                this.updateDeleteButtonState();

                // 페이징 정보 저장
                this.totalPages = data.page.totalPages;
                this.totalElements = data.page.totalElements;
                this.currentPage = data.page.number;

                // 이메일 데이터가 없는 경우 메시지 표시
                if (data._embedded && data._embedded.emailQueues && data._embedded.emailQueues.length > 0) {
                    this.shadowRoot.getElementById('no-emails-message').style.display = 'none';
                    this.shadowRoot.getElementById('emailTable').style.display = 'table';

                    // 이메일 목록 생성
                    data._embedded.emailQueues.forEach(email => {
                        const row = document.createElement('tr');

                        // ID를 URL에서 추출
                        const emailId = EmailUtils.getEmailIdFromUrl(email._links.self.href);
                        const emailUrl = email._links.self.href;

                        row.innerHTML = `
            <td><input type="checkbox" class="email-checkbox" data-url="${emailUrl}"></td>
            <td>${emailId}</td>
            <td>${email.sender}</td>
            <td>${email.recipient}</td>
            <td><a class="email-title-link" data-url="${emailUrl}">${email.subject}</a></td>
            <td>
              <button class="action-button edit-button">수정</button>
            </td>
          `;

                        // 이벤트 추가
                        const checkbox = row.querySelector('.email-checkbox');
                        checkbox.addEventListener('change', (e) => {
                            if (e.target.checked) {
                                this.selectedEmails.add(emailUrl);
                            } else {
                                this.selectedEmails.delete(emailUrl);
                                // 전체 선택 체크박스도 해제
                                this.shadowRoot.getElementById('selectAllCheckbox').checked = false;
                            }
                            this.updateDeleteButtonState();
                        });

                        const titleLink = row.querySelector('.email-title-link');
                        titleLink.addEventListener('click', () => {
                            EmailUtils.dispatchEmailEvent('show-email-details', {emailUrl});
                        });

                        const editBtn = row.querySelector('.edit-button');
                        editBtn.addEventListener('click', () => {
                            EmailUtils.dispatchEmailEvent('show-email-edit-form', {emailUrl});
                        });

                        emailTableBody.appendChild(row);
                    });
                } else {
                    this.shadowRoot.getElementById('no-emails-message').style.display = 'block';
                    this.shadowRoot.getElementById('emailTable').style.display = 'none';
                }

                // 페이징 생성
                this.createPagination(this.totalPages, this.currentPage);
            })
            .catch(error => {
                console.error('이메일 목록을 불러오는 중 오류 발생:', error);
                alert('이메일 목록을 불러오는 데 실패했습니다. 페이지를 새로고침하거나 나중에 다시 시도해주세요.');
            });
    }

    createPagination(totalPages, currentPage) {
        const pagination = this.shadowRoot.getElementById('pagination');
        pagination.innerHTML = '';

        if (totalPages <= 0) {
            return;
        }

        const ul = document.createElement('ul');
        ul.className = 'pagination-list';

        // 처음 페이지 버튼
        const firstLi = document.createElement('li');
        firstLi.className = currentPage === 0 ? 'disabled' : '';
        const firstLink = document.createElement('a');
        firstLink.href = "javascript:void(0)";
        firstLink.textContent = '처음';
        if (currentPage !== 0) {
            firstLink.addEventListener('click', () => this.loadEmails(0, this.pageSize));
        }
        firstLi.appendChild(firstLink);
        ul.appendChild(firstLi);

        // 이전 페이지 버튼
        const prevLi = document.createElement('li');
        prevLi.className = currentPage === 0 ? 'disabled' : '';
        const prevLink = document.createElement('a');
        prevLink.href = "javascript:void(0)";
        prevLink.textContent = '이전';
        if (currentPage !== 0) {
            prevLink.addEventListener('click', () => this.loadEmails(currentPage - 1, this.pageSize));
        }
        prevLi.appendChild(prevLink);
        ul.appendChild(prevLi);

        // 페이지 번호 버튼 (최대 5개만 표시)
        const startPage = Math.max(0, Math.min(currentPage - 2, totalPages - 5));
        const endPage = Math.min(startPage + 5, totalPages);

        for (let i = startPage; i < endPage; i++) {
            const pageLi = document.createElement('li');
            pageLi.className = i === currentPage ? 'active' : '';

            const pageLink = document.createElement('a');
            pageLink.href = "javascript:void(0)";
            pageLink.textContent = i + 1;

            if (i !== currentPage) {
                pageLink.addEventListener('click', () => this.loadEmails(i, this.pageSize));
            }

            pageLi.appendChild(pageLink);
            ul.appendChild(pageLi);
        }

        // 다음 페이지 버튼
        const nextLi = document.createElement('li');
        nextLi.className = currentPage >= totalPages - 1 ? 'disabled' : '';
        const nextLink = document.createElement('a');
        nextLink.href = "javascript:void(0)";
        nextLink.textContent = '다음';
        if (currentPage < totalPages - 1) {
            nextLink.addEventListener('click', () => this.loadEmails(currentPage + 1, this.pageSize));
        }
        nextLi.appendChild(nextLink);
        ul.appendChild(nextLi);

        // 마지막 페이지 버튼
        const lastLi = document.createElement('li');
        lastLi.className = currentPage >= totalPages - 1 ? 'disabled' : '';
        const lastLink = document.createElement('a');
        lastLink.href = "javascript:void(0)";
        lastLink.textContent = '마지막';
        if (currentPage < totalPages - 1) {
            lastLink.addEventListener('click', () => this.loadEmails(totalPages - 1, this.pageSize));
        }
        lastLi.appendChild(lastLink);
        ul.appendChild(lastLi);

        pagination.appendChild(ul);
    }
}

customElements.define('email-list', EmailList);