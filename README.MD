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
