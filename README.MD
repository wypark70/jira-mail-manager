# Jira mail manager

# Windows 환경에서 Git 연결 문제 해결 가이드

## 문제 상황
- 브라우저로는 GitHub 저장소 접근 가능
- Git 명령어로는 접근 불가능
- IP가 최근에 변경됨

## 자격 증명 관리자에서 삭제해도 인증 프롬프트가 나타나지 않는 경우

### 1. Git Credential Helper 초기화
```bash
# 시스템 수준 credential helper 제거
git config --system --unset credential.helper

# 전역 수준 credential helper 제거
git config --global --unset credential.helper
```

### 2. 현재 설정 확인
```bash
# Git 인증 설정 확인
git config --list | findstr credential

# Git 설정 파일 위치 확인
git config --list --show-origin
```

### 3. Windows 자격 증명 정리
```bash
# Git 관련 자격 증명 목록 확인
cmdkey /list | findstr git
```

### 4. Git Credential Manager 재설정
```bash
# Credential Manager Core 삭제
git credential-manager-core delete https://github.com

# Credential Manager 제거
git credential-manager-core uninstall

# Credential Manager 재설치
git credential-manager-core install
```

### 5. 로컬 Git 설정 초기화
```bash
git config --local --unset credential.helper
```

### 6. 환경 변수 확인
```bash
# HOME 환경변수 확인
echo %HOME%

# Git 설치 경로 확인
where git
```

IP 변경 이후 브라우저에서는 GitHub 저장소에 접근이 가능하지만, Git 명령어로는 접근할 수 없는 경우 다음을 점검해 보세요:

### 1. **SSH 연결 확인**
- SSH 방식으로 GitHub에 연결하는 경우, IP 변경으로 인해 SSH 키 인증이 제대로 되지 않을 가능성이 있습니다.
- 다음 명령어를 실행하여 GitHub과 SSH 연결을 테스트하세요:
  ```powershell
  ssh -T git@github.com
  ```
  - 정상적으로 인증되면 `"Hi <username>! You've successfully authenticated, but GitHub does not provide shell access."` 메시지가 나타납니다.
  - `Permission denied (publickey)` 오류가 발생하면 SSH 키 설정을 다시 점검해야 합니다.

### 2. **SSH 키 재등록**
- 변경된 IP에서 인증 문제가 발생할 수 있으므로, SSH 키를 다시 등록해 보세요:
  ```powershell
  eval "$(ssh-agent -s)"
  ssh-add $HOME\.ssh\id_rsa
  ```
- 이후 GitHub의 **SSH 키 설정 페이지** (`https://github.com/settings/keys`)에서 기존 SSH 키가 올바르게 등록되어 있는지 확인하세요.

### 3. **GitHub 자격 증명 초기화**
- Windows의 경우, 자격 증명 관리자에서 GitHub 관련 항목을 삭제한 후 다시 시도해 보세요:
  ```powershell
  git credential reject https://github.com
  ```
- 인증 프롬프트가 나타나지 않는다면, GitHub에서 **Personal Access Token (PAT)**을 생성하여 인증해야 할 수도 있습니다.

### 4. **원격 저장소 URL 확인**
- SSH 또는 HTTPS 방식의 URL이 올바르게 설정되어 있는지 확인하세요:
  ```powershell
  git remote -v
  ```
- 필요 시 아래 명령어로 원격 URL을 수정하세요:
  ```powershell
  git remote set-url origin git@github.com:사용자명/저장소명.git
  ```
  또는
  ```powershell
  git remote set-url origin https://github.com/사용자명/저장소명.git
  ```

### 5. **네트워크 및 방화벽 점검**
- 변경된 IP 환경에서 SSH 또는 HTTPS 트래픽이 차단되었을 가능성이 있습니다.
- **GitHub 서버 연결 테스트**:
  ```powershell
  Test-NetConnection github.com -Port 22
  ```
- VPN을 사용 중이라면, 일시적으로 비활성화하고 다시 테스트해 보세요.

위 방법을 하나씩 점검해 보시면 문제 해결에 도움이 될 것입니다! 그래도 접근이 어렵다면 오류 메시지를 공유해 주세요. 🚀
