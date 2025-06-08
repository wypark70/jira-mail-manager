# Jira mail manager

본 애플리케이션은 Jira에서 특정 이벤트 발생 시 자동으로 발송되는 이메일을 가로채어 관리하고, 정의된 로직에 따라 실제 메일 발송을 처리하는 시스템입니다.
Jira 시스템이 발송하는 이메일을 내장된 SMTP 서버를 통해 수신한 뒤, 즉시 외부로 발송하는 대신 데이터베이스의 `EmailQueue` 테이블에 저장합니다.
이후, 별도의 배치 작업(`EmailQueueBatchService`)이 주기적으로 이 큐를 확인하여, 저장된 이메일 정보를 바탕으로 실제 메일 발송 작업을 수행하고 그 결과를 업데이트합니다.
성공적으로 처리된 이메일은 또 다른 배치 작업(`MoveToHistoryBatchService`)에 의해 `EmailHistory` 테이블로 옮겨져 관리됩니다.

## 주요 기능

*   내장 SMTP 서버를 통한 메일 수신
*   **이메일 큐 관리 (`EmailQueue`)**:
    *   수신된 모든 이메일(발신자, 수신자, 제목, 본문, 첨부파일 등)을 일시적으로 보관합니다.
    *   이메일 처리 상태(`WAITING`, `PROCESSING`, `SENT`, `FAILED`, `RETRY`, `SCHEDULED`)를 관리합니다.
*   **이메일 처리 배치 (`EmailQueueBatchService`)**:
    *   주기적으로 `EmailQueue`에서 처리 대기 중인 이메일을 가져와 비동기 방식으로 일괄 처리합니다.
    *   처리 중 잠금(Lock) 메커니즘을 사용하여 중복 처리를 방지합니다.
    *   처리 결과(성공, 실패, 재시도 필요)에 따라 이메일 상태를 업데이트합니다.
    *   실패 시 재시도 로직을 포함하며, 재시도 횟수 및 간격을 관리합니다.
    *   특정 시간에 발송되도록 예약된 이메일을 처리합니다.
*   **이메일 이력 관리 (`MoveToHistoryBatchService`)**:
    *   `EmailQueue`에서 성공적으로 처리된 (`SENT` 상태) 이메일들을 주기적으로 `EmailHistory` 테이블로 이동시켜 장기 보관합니다.
*   **동적 설정 및 관리**:
    *   Spring Boot Actuator (`/actuator/health`, `/actuator/info`, `/actuator/refresh`)를 통한 애플리케이션 모니터링 및 관리 기능을 제공합니다.
    *   `@RefreshScope`를 이용하여 배치 작업의 Cron 표현식 등 주요 설정을 애플리케이션 재시작 없이 동적으로 변경할 수 있습니다.
*   **API 제공**:
    *   배치 작업의 수동 트리거 및 상태 조회를 위한 REST API 엔드포인트를 제공합니다.
    *   Spring Data REST를 통해 주요 데이터 모델(`EmailQueue` 등)에 대한 RESTful API를 제공합니다.

## 기술 스택

*   Java 17
*   Spring Boot 3.2.5
*   Spring Cloud 2023.0.2
*   Spring Data JPA
*   H2 Database (기본, 다른 DB로 변경 가능)
*   SubEthaSMTP (내장 SMTP 서버)
*   Unirest-Java (HTTP 클라이언트)
*   Lombok
*   Gradle

## 빌드 및 실행

### Prerequisites

*   JDK 17 이상
*   Gradle (프로젝트 내 `gradlew` 사용 권장)

### 빌드

```bash
./gradlew bootJar
```
빌드된 JAR 파일은 `spring-boot/build/libs/jira-mail-manager-1.0.0.jar` 경로에 생성됩니다.

### 실행

기본적으로 `prod` 프로파일로 실행됩니다. (`application-prod.yml` 설정 사용)
```bash
java -jar spring-boot/build/libs/jira-mail-manager-1.0.0.jar
```

로컬 개발 환경에서는 `local` 프로파일을 사용하여 실행할 수 있습니다. (`application-local.yml` 설정 사용)
```bash
./gradlew bootRun
```

다른 프로파일로 실행하려면:
```bash
java -jar spring-boot/build/libs/jira-mail-manager-1.0.0.jar --spring.profiles.active=dev
```

## 설정

주요 설정은 `spring-boot/src/main/resources/` 경로의 `application.yml` 파일 및 프로파일별 `application-{profile}.yml` 파일에서 관리합니다.

*   **SMTP 설정**: `mail.smtp.*` (허용 IP, 도메인, 첨부파일 경로 등)
*   **배치 설정**: `mail.batch.*` (배치 크기, 스케줄 Cron 표현식 등)
*   **데이터베이스 설정**: `spring.datasource.*`
*   **JPA 설정**: `spring.jpa.*`

## API 엔드포인트

*   **Actuator 엔드포인트** (기본 경로: `/actuator`):
    *   `/actuator/health`: 애플리케이션 상태 확인
    *   `/actuator/info`: 애플리케이션 정보
    *   `/actuator/refresh`: `@RefreshScope` 빈 설정 새로고침 (POST 요청)
*   **이메일 큐 관리 API** (기본 경로: `/api/email-queue`):
    *   `POST /`: 새 이메일 큐에 추가
    *   `POST /schedule`: 예약 이메일 큐에 추가
    *   `DELETE /{id}`: 큐에서 특정 이메일 취소
    *   `GET /stats`: 이메일 큐 통계 조회
    *   `GET /stats/stream`: 실시간 통계 SSE 스트림
    *   `GET /status/{status}`: 특정 상태의 이메일 목록 조회
    *   `POST /{id}/retry`: 특정 실패 이메일 재시도
    *   `POST /retry`: 실패한 모든 이메일 재시도
    *   `GET /{id}`: 특정 이메일 상세 정보 조회
    *   `GET /search`: 이메일 검색 및 필터링
    *   `POST /move-to-history/{status}`: 특정 상태의 이메일을 히스토리로 이동
*   **이메일 큐 배치 API** (기본 경로: `/api/email-queue-batch`):
    *   `POST /start`: 이메일 큐 처리 배치 시작
    *   `POST /stop`: 이메일 큐 처리 배치 중지
    *   `GET /status`: 이메일 큐 처리 배치 상태 조회
    *   `POST /process`: 이메일 큐 수동 처리
*   **이메일 히스토리 이동 배치 API** (기본 경로: `/api/move-to-history-batch`):
    *   `POST /start`: 히스토리 이동 배치 시작
    *   `POST /stop`: 히스토리 이동 배치 중지
    *   `GET /status`: 히스토리 이동 배치 상태 조회
*   **이메일 히스토리 API** (기본 경로: `/api/email-history`):
    *   `GET /{id}`: 특정 이메일 히스토리 상세 조회
    *   `GET /search`: 이메일 히스토리 검색 및 필터링
    *   `DELETE /delete-all`: 모든 이메일 히스토리 삭제
*   **SMTP 인터셉터 API** (기본 경로: `/api/smtp-interceptor`):
    *   `GET /logs`: SMTP 로그 SSE 스트림
    *   `POST /start`: 내장 SMTP 서버 시작
    *   `POST /stop`: 내장 SMTP 서버 중지
    *   `GET /status`: 내장 SMTP 서버 상태 조회
*   **Spring Data REST 엔드포인트** (기본 경로: `/api/data`):
    *   JPA 엔티티에 대한 RESTful API 제공 (예: `/api/data/emailQueues`)


## H2 데이터베이스 콘솔

애플리케이션 실행 중 다음 경로로 H2 데이터베이스 콘솔에 접근할 수 있습니다 (기본 설정 시):
`http://localhost:8080/h2-console`
*   JDBC URL: `jdbc:h2:file:./data/mail-manager` (또는 `application.yml`에 설정된 값)
*   Username: `sa`
*   Password: (비어 있음)
