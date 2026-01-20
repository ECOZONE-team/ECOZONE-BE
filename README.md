# ECOZONE Backend API

> Spring Boot 3.5 기반 헥사고날 아키텍처를 적용한 RESTful API 서버

## 📋 프로젝트 개요

ECOZONE Backend API는 인증/인가, 문의 관리, 결제 처리 기능을 제공하는 엔터프라이즈급 백엔드 시스템입니다.
헥사고날 아키텍처(Ports & Adapters)와 도메인 주도 설계(DDD) 원칙을 적용하여 확장 가능하고 유지보수가 용이한 구조로 설계되었습니다.

## ✨ 주요 기능

### 🔐 인증 및 인가 (Authentication & Authorization)
- JWT 기반 Stateless 인증
- Access Token / Refresh Token 로테이션
- 역할 기반 접근 제어 (RBAC: USER, ADMIN)
- Redis 기반 토큰 무효화 관리
- CORS 정책 관리

### 📝 문의 관리 (Inquiry Management)
- 고객 문의 생성 및 조회
- 페이지네이션 지원
- 역할별 권한 관리 (사용자: 본인 문의만, 관리자: 전체 조회)
- 문의 답변 상태 관리

### 💳 결제 처리 (Payment Processing)
- Toss Payments PG 연동
- 2-Phase 멱등성 결제 승인
- 결제 취소 및 환불
- Webhook 이벤트 처리
- 이벤트 소싱 패턴 적용
- 복식부기 장부 시스템
- 자동 결제 조정(Reconciliation)
- 사용자/가맹점 지갑 관리

## 🛠 기술 스택

### Core Framework
- **Java 23** (Preview Features)
- **Spring Boot 3.5.3**
- **Spring Security** (OAuth2, JWT)
- **Spring Data JPA** & **Spring Data Redis**
- **Spring WebFlux** (Reactive Web)

### Database & Cache
- **MySQL** - 주 데이터베이스
- **Redis** - 토큰 관리 및 캐싱

### Build & DevOps
- **Gradle** - 빌드 도구
- **Docker** - 컨테이너화
- **Docker Compose** - 로컬 개발 환경

### Libraries
- **Lombok** 1.18.x - 보일러플레이트 코드 제거
- **MapStruct** 1.4.2 - DTO/Entity 매핑
- **SpringDoc OpenAPI** 2.8.6 - API 문서 자동화
- **JUnit 5** - 테스트 프레임워크

## 🚀 시작하기

### 사전 요구사항
- JDK 23 이상
- Docker & Docker Compose
- MySQL 8.x
- Redis 7.x

### 로컬 실행

1. **저장소 클론**
```bash
git clone <repository-url>
cd ECOZONE-BE
```

2. **환경 설정**
```bash
# application.yml 파일 생성 (예시)
cp /srv/app/application.yml.example /srv/app/application.yml
```

필수 설정 항목:
- 데이터베이스 연결 정보
- Redis 연결 정보
- JWT 비밀키 및 정책
- Toss Payments API 키

3. **Docker Compose로 실행**
```bash
docker-compose up -d
```

4. **Gradle로 빌드 및 실행**
```bash
./gradlew clean build
./gradlew bootRun
```

애플리케이션이 `http://localhost:8080`에서 실행됩니다.

### API 문서 확인
Swagger UI: `http://localhost:8080/swagger-ui.html`

## 📡 API 엔드포인트

### 인증 API (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/signup` | 회원가입 | ❌ |
| POST | `/api/auth/login` | 로그인 (토큰 발급) | ❌ |
| POST | `/api/auth/refresh` | 토큰 갱신 | ✅ |
| POST | `/api/auth/logout` | 로그아웃 (토큰 무효화) | ✅ |

### 문의 API (`/api/inquiries`)
| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| POST | `/api/inquiries` | 문의 생성 | USER |
| GET | `/api/inquiries` | 문의 목록 조회 (페이징) | AUTHENTICATED |
| GET | `/api/inquiries/{id}` | 문의 상세 조회 | AUTHENTICATED |
| PATCH | `/api/inquiries/{id}` | 문의 수정 | USER (본인) |
| POST | `/api/inquiries/{id}/answer` | 문의 답변 완료 처리 | ADMIN |

### 결제 API (`/api/payments`)
| Method | Endpoint | Description | Header Required |
|--------|----------|-------------|-----------------|
| POST | `/api/payments/orders` | 주문 생성 | - |
| POST | `/api/payments/confirm` | 결제 승인 | `Idempotency-Key` |
| POST | `/api/payments/{paymentId}/cancel` | 결제 취소 | - |
| POST | `/api/payments/webhook` | PG 웹훅 수신 | - |

## 🏗 아키텍처

### 헥사고날 아키텍처 (Ports & Adapters)

```
┌─────────────────────────────────────────┐
│         Infrastructure Layer            │
│  (Web, Persistence, Security, External) │
└───────────────┬─────────────────────────┘
                │ Adapters
┌───────────────▼─────────────────────────┐
│         Application Layer               │
│   (Use Cases, Services, DTOs, Ports)    │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│           Domain Layer                  │
│  (Aggregates, Entities, Value Objects)  │
└─────────────────────────────────────────┘
```

### 모듈 구조
```
co.ecozone.ecozoneapi/
├── auth/              # 인증/인가 모듈
├── inquiry/           # 문의 관리 모듈
├── payment/           # 결제 처리 모듈
├── platform/          # 플랫폼 공통 (에러 처리, 문서화)
└── common/            # 공통 설정
```

### 주요 패턴
- **Aggregate Root Pattern** - 일관성 경계 정의
- **Repository Pattern** - 데이터 접근 추상화
- **Event Sourcing** - 결제 이벤트 추적
- **State Machine** - 결제 상태 관리
- **Idempotency** - 안전한 재시도 보장
- **Factory Methods** - 객체 생성 제어

## 🔒 보안

### JWT 인증
- **Access Token**: 단기 유효 (예: 15분)
- **Refresh Token**: 장기 유효 (예: 7일)
- **Token Rotation**: Refresh 시 새로운 토큰 쌍 발급
- **Token Revocation**: Redis 기반 블랙리스트

### 역할 기반 접근 제어
```java
@PreAuthorize("hasRole('ADMIN')")  // 관리자만
@PreAuthorize("hasRole('USER')")   // 일반 사용자
@PreAuthorize("isAuthenticated()") // 인증된 사용자
```

### CORS 정책
- 패턴 기반 Origin 검증
- 인증 정보 포함 지원
- 설정 가능한 허용 메서드/헤더

## 📊 데이터베이스 스키마

### 주요 엔티티

#### Users (사용자)
- 고유 이메일 기반 인증
- 역할 기반 권한 관리
- 계정 활성화 상태 관리

#### Payment (결제)
- 낙관적 잠금 (@Version)
- 주문 ID 기반 멱등성
- 상태 기계 기반 상태 전이

#### Inquiry (문의)
- 회사별 문의 관리
- 답변 상태 추적
- 사용자 소유권 관리

#### PaymentEvent (결제 이벤트)
- 이벤트 소싱 패턴
- PG 웹훅 이벤트 추적

#### LedgerEntry (장부)
- 복식부기 시스템
- 재무 감사 추적

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :auth:test
./gradlew :payment:test

# 통합 테스트
./gradlew integrationTest
```

## 📦 빌드 및 배포

### Docker 이미지 빌드
```bash
docker build -t ecozone-api:latest .
```

### 프로덕션 배포
```bash
# JAR 빌드
./gradlew clean bootJar

# 실행
java -jar build/libs/ecozone-api-0.0.1-SNAPSHOT.jar
```

## 🔧 개발 가이드

자세한 개발 가이드는 [DEVELOPMENT.md](./DEVELOPMENT.md)를 참조하세요.

## 📝 라이선스

이 프로젝트는 비공개 소프트웨어입니다.

## 👥 팀

ECOZONE Development Team

## 📞 문의

프로젝트 관련 문의사항은 이슈 트래커를 이용해주세요.
