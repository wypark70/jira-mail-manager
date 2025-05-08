# jira-mail-manager

## 참고 사이트
- https://next.shadcn-svelte.com/docs/installation/sveltekit

## 터미널에서 한글 깨짐 방짐
```shell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; [Console]::InputEncoding = [System.Text.Encoding]::UTF8; chcp EUC-KR
```

# PowerShell 프로필에 한글 인코딩(UTF-8) 설정 추가 방법

## 1. PowerShell 프로필 파일 위치 확인
아래 명령어를 입력하면 현재 사용자의 PowerShell 프로필 경로가 출력됩니다.

```powershell
echo $PROFILE
```

## 2. 프로필 파일이 없으면 생성
아래 명령어를 입력하면 프로필 파일이 자동으로 생성됩니다.

```powershell
if (!(Test-Path -Path $PROFILE)) { New-Item -ItemType File -Path $PROFILE -Force }
```

## 3. 프로필 파일 열기
아래 명령어로 메모장 등으로 프로필 파일을 엽니다.

```powershell
notepad $PROFILE
```

## 4. 아래 내용을 파일 맨 아래에 추가 후 저장

```powershell
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
```

## 5. PowerShell을 완전히 종료 후 새로 실행

---

이렇게 하면 앞으로 PowerShell을 실행할 때마다 한글이 깨지지 않고 정상적으로 표시됩니다! 