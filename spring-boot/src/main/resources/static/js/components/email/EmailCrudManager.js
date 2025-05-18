// EmailCrudManager.js - 이메일 CRUD 매니저 컴포넌트

export class EmailCrudManager extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({mode: 'open'});
        this.render();
    }

    render() {
        this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
          width: 100%;
        }
      </style>
      
      <div class="email-manager-container">
        <email-list></email-list>
        <email-add></email-add>
        <email-view></email-view>
        <email-edit></email-edit>
        <email-delete></email-delete>
      </div>
    `;
    }
}

customElements.define('email-crud-manager', EmailCrudManager);
