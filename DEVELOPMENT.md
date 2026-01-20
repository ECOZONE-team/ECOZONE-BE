# ECOZONE Backend - 개발 가이드

> 개발자를 위한 상세 개발 가이드 문서

## 목차
- [개발 환경 설정](#개발-환경-설정)
- [프로젝트 구조](#프로젝트-구조)
- [아키텍처 가이드](#아키텍처-가이드)
- [코딩 컨벤션](#코딩-컨벤션)
- [데이터베이스 가이드](#데이터베이스-가이드)
- [보안 가이드](#보안-가이드)
- [테스트 가이드](#테스트-가이드)
- [API 개발 가이드](#api-개발-가이드)
- [배포 가이드](#배포-가이드)
- [문제 해결](#문제-해결)

## 개발 환경 설정

### 필수 도구 설치

#### 1. JDK 23 설치
```bash
# SDKMAN 사용 (권장)
sdk install java 23-open

# 설치 확인
java -version
# 출력: openjdk version "23" ...
```

#### 2. Docker & Docker Compose 설치
```bash
# Docker 설치 (Ubuntu/Debian)
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Docker Compose V2
docker compose version
```

#### 3. IDE 설정 (IntelliJ IDEA 권장)

**플러그인 설치:**
- Lombok Plugin
- MapStruct Support
- Spring Boot
- Database Navigator

**설정:**
```
Settings → Build, Execution, Deployment → Compiler → Annotation Processors
  ☑ Enable annotation processing
```

#### 4. Git Hooks 설정 (선택사항)
```bash
# pre-commit hook 설정
cp .git-hooks/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit
```

### 로컬 개발 환경 구성

#### 1. 저장소 클론 및 브랜치 설정
```bash
git clone <repository-url>
cd ECOZONE-BE
git checkout develop
```

#### 2. 환경 변수 설정

`/srv/app/application.yml` 파일 생성:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecozone?useSSL=false&allowPublicKeyRetrieval=true
    username: ecozone_user
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379

  jpa:
    hibernate:
      ddl-auto: validate  # 개발: update, 프로덕션: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

jwt:
  secret: your-secret-key-min-256-bits
  access-token-ttl: 900000    # 15분
  refresh-token-ttl: 604800000  # 7일
  issuer: ecozone-api
  subject: auth-token

payment:
  toss:
    client-key: test_ck_xxxxxxxxxx
    secret-key: test_sk_xxxxxxxxxx
    security-key: your_webhook_security_key
    base-url: https://api.tosspayments.com

cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:5173
  allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true

logging:
  level:
    co.ecozone.ecozoneapi: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 3. Docker Compose로 의존성 실행
```bash
# Redis 및 MySQL 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 상태 확인
docker-compose ps
```

#### 4. 데이터베이스 초기화
```bash
# MySQL 접속
docker exec -it <mysql-container-id> mysql -u root -p

# 데이터베이스 및 사용자 생성
CREATE DATABASE ecozone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ecozone_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ecozone.* TO 'ecozone_user'@'%';
FLUSH PRIVILEGES;
```

#### 5. 애플리케이션 실행
```bash
# Gradle로 실행
./gradlew bootRun

# 또는 IDE에서 EcozoneApiApplication.main() 실행
```

#### 6. Redis Insight로 Redis 모니터링
```
URL: http://localhost:5540
```

## 프로젝트 구조

### 디렉터리 구조
```
ECOZONE-BE/
├── src/
│   ├── main/
│   │   ├── java/co/ecozone/ecozoneapi/
│   │   │   ├── auth/                      # 인증/인가 모듈
│   │   │   │   ├── application/          # 애플리케이션 계층
│   │   │   │   │   ├── command/          # 커맨드 DTO
│   │   │   │   │   ├── port/             # 포트 인터페이스
│   │   │   │   │   ├── service/          # 유스케이스 서비스
│   │   │   │   │   └── usecase/          # 유스케이스 인터페이스
│   │   │   │   ├── domain/               # 도메인 계층
│   │   │   │   │   ├── model/            # 애그리게이트, 엔티티, VO
│   │   │   │   │   ├── repository/       # 레포지토리 인터페이스
│   │   │   │   │   └── security/         # 도메인 보안 정책
│   │   │   │   └── infrastructure/       # 인프라 계층
│   │   │   │       ├── persistence/      # JPA 엔티티, 레포지토리 구현
│   │   │   │       ├── security/         # Spring Security 설정
│   │   │   │       │   ├── filter/       # 필터 (JWT 검증 등)
│   │   │   │       │   ├── provider/     # 인증 제공자
│   │   │   │       │   └── SecurityConfig.java
│   │   │   │       └── web/              # 컨트롤러, DTO
│   │   │   ├── inquiry/                   # 문의 관리 모듈
│   │   │   │   └── [동일한 3-tier 구조]
│   │   │   ├── payment/                   # 결제 처리 모듈
│   │   │   │   └── [동일한 3-tier 구조]
│   │   │   ├── platform/                  # 플랫폼 계층
│   │   │   │   ├── doc/                  # OpenAPI/Swagger 설정
│   │   │   │   └── web/error/            # 글로벌 예외 처리
│   │   │   └── common/                    # 공통 설정
│   │   │       └── config/               # 공통 Configuration
│   │   └── resources/
│   │       ├── application.yml            # 애플리케이션 설정
│   │       └── logback-spring.xml         # 로깅 설정
│   └── test/                              # 테스트 코드
├── build.gradle                           # Gradle 빌드 스크립트
├── Dockerfile                             # Docker 이미지 빌드
├── compose.yml                            # Docker Compose 설정
├── README.md                              # 프로젝트 README
└── DEVELOPMENT.md                         # 이 문서
```

### 모듈별 책임

#### Auth Module
- 사용자 회원가입/로그인
- JWT 토큰 발급 및 검증
- 토큰 갱신 및 무효화
- 역할 기반 접근 제어

#### Inquiry Module
- 고객 문의 생성/조회/수정
- 문의 답변 상태 관리
- 권한 기반 문의 필터링

#### Payment Module
- 결제 주문 생성
- 2-Phase 결제 승인
- 결제 취소 및 환불
- PG 웹훅 처리
- 결제 이벤트 추적
- 자동 조정 시스템

## 아키텍처 가이드

### 헥사고날 아키텍처 (Ports & Adapters)

#### 계층별 역할

**1. Domain Layer (도메인 계층)**
- 순수 비즈니스 로직만 포함
- 외부 의존성 없음 (프레임워크, 데이터베이스 등)
- 도메인 이벤트, 애그리게이트, 엔티티, 값 객체

```java
// 예시: Payment Aggregate
@Getter
public class Payment {
    private Long id;
    private String orderId;  // 비즈니스 식별자
    private long amount;
    private PaymentStatus status;  // 상태 기계

    // Factory Method
    public static Payment initiated(String orderId, long amount, UserId userId) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.INITIATED;
        payment.createdAt = Instant.now();
        return payment;
    }

    // 비즈니스 로직: 상태 전이
    public void authorize(String paymentKey) {
        if (!status.canTransitionTo(PaymentStatus.AUTHORIZED)) {
            throw new IllegalStateTransitionException(...);
        }
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.AUTHORIZED;
    }
}
```

**2. Application Layer (애플리케이션 계층)**
- 유스케이스 조율
- 트랜잭션 경계 정의
- 도메인 객체 간 조정

```java
// 예시: ConfirmPaymentService (유스케이스)
@Service
@RequiredArgsConstructor
public class ConfirmPaymentService {
    private final LoadPaymentPort loadPaymentPort;
    private final SavePaymentPort savePaymentPort;
    private final TossPaymentsPort tossPaymentsPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ConfirmPaymentResponse confirm(ConfirmPaymentCommand command) {
        // 1. 멱등성 체크
        // 2. 도메인 로직 실행
        Payment payment = loadPaymentPort.loadByOrderId(command.orderId());
        payment.authorize(command.paymentKey());

        // 3. PG사 승인 요청
        TossConfirmResponse pgResponse = tossPaymentsPort.confirm(...);

        // 4. 결제 확정
        payment.confirm();
        savePaymentPort.save(payment);

        return ConfirmPaymentResponse.from(payment);
    }
}
```

**3. Infrastructure Layer (인프라 계층)**
- 외부 시스템 연동 (DB, Redis, 외부 API)
- 웹 어댑터 (컨트롤러)
- 영속성 어댑터 (JPA 레포지토리)

```java
// 예시: PaymentPersistenceAdapter
@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements LoadPaymentPort, SavePaymentPort {
    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    @Override
    public Payment loadByOrderId(String orderId) {
        PaymentJpaEntity entity = jpaRepository.findByOrderId(orderId)
            .orElseThrow(() -> new PaymentNotFoundException(...));
        return mapper.toDomain(entity);
    }

    @Override
    public void save(Payment payment) {
        PaymentJpaEntity entity = mapper.toEntity(payment);
        jpaRepository.save(entity);
    }
}
```

### 의존성 방향
```
Infrastructure → Application → Domain
       ↓             ↓
    Adapters ←→ Ports (인터페이스)
```

**핵심 원칙:**
- Domain은 어떤 것도 의존하지 않음
- Application은 Domain만 의존
- Infrastructure는 Application/Domain을 의존
- **의존성 역전 원칙**: Infrastructure가 Application의 Port 인터페이스를 구현

### 새로운 기능 추가 시 흐름

#### 예시: 새로운 "상품(Product)" 모듈 추가

**1. Domain Layer 작성**
```java
// 1.1 Aggregate Root
public class Product {
    private ProductId id;
    private String name;
    private Money price;
    private ProductStatus status;

    public static Product create(String name, Money price) {
        // 생성 로직
    }

    public void activate() {
        // 비즈니스 로직
    }
}

// 1.2 Repository 인터페이스 (Port)
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
}
```

**2. Application Layer 작성**
```java
// 2.1 Command DTO
public record CreateProductCommand(String name, long priceAmount) {}

// 2.2 Use Case Service
@Service
@RequiredArgsConstructor
public class CreateProductService {
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(CreateProductCommand command) {
        Product product = Product.create(
            command.name(),
            Money.of(command.priceAmount())
        );

        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }
}
```

**3. Infrastructure Layer 작성**
```java
// 3.1 JPA Entity
@Entity
@Table(name = "product")
class ProductJpaEntity {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private Long priceAmount;
    // ...
}

// 3.2 JPA Repository
interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {}

// 3.3 Persistence Adapter (Port 구현)
@Component
@RequiredArgsConstructor
class ProductPersistenceAdapter implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toEntity(product);
        ProductJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}

// 3.4 Web Adapter (Controller)
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class ProductController {
    private final CreateProductService createProductService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @RequestBody @Valid CreateProductRequest request
    ) {
        CreateProductCommand command = request.toCommand();
        ProductResponse response = createProductService.create(command);
        return ResponseEntity.ok(response);
    }
}
```

## 코딩 컨벤션

### Java 코드 스타일

#### 1. 네이밍 규칙
```java
// 클래스: PascalCase
public class PaymentService {}

// 메서드/변수: camelCase
public void processPayment() {}
private String orderId;

// 상수: UPPER_SNAKE_CASE
public static final int MAX_RETRY_COUNT = 3;

// 패키지: lowercase
package co.ecozone.ecozoneapi.payment;
```

#### 2. Domain Model 명명
```java
// Aggregate Root: 명사
public class Payment {}
public class User {}

// Value Object: 명사 + 의미
public class UserId {}
public class Money {}

// Domain Service: 동사 + Service (최소화)
public class PaymentReconciliationService {}

// Domain Event: 과거형
public class PaymentConfirmedEvent {}
```

#### 3. Application Layer 명명
```java
// Use Case Service: 동사 + 명사 + Service
public class ConfirmPaymentService {}
public class CreateInquiryService {}

// Command: 동사 + 명사 + Command
public record ConfirmPaymentCommand(...) {}

// Response: 명사 + Response
public record PaymentResponse(...) {}

// Port Interface: 동사 + 명사 + Port
public interface LoadPaymentPort {}
public interface SavePaymentPort {}
```

#### 4. Infrastructure Layer 명명
```java
// Adapter: 명사 + 구현체 타입 + Adapter
public class PaymentPersistenceAdapter {}
public class TossPaymentsAdapter {}

// Controller: 명사 + Controller
public class PaymentController {}

// JPA Entity: 명사 + JpaEntity
class PaymentJpaEntity {}

// JPA Repository: 명사 + JpaRepository
interface PaymentJpaRepository extends JpaRepository<...> {}
```

### 코드 구조 원칙

#### 1. Immutability 선호
```java
// ✅ Good: Record 사용
public record CreateUserCommand(String email, String name, String password) {}

// ✅ Good: 불변 객체
@Getter
public class User {
    private final UserId id;
    private final String email;

    // 변경은 새 인스턴스 반환
    public User withEmail(String newEmail) {
        return new User(this.id, newEmail, this.name);
    }
}

// ❌ Bad: Setter 사용
public class User {
    private String email;
    public void setEmail(String email) { this.email = email; }  // 피하기
}
```

#### 2. Factory Method 사용
```java
// ✅ Good
public class Payment {
    private Payment() {}  // private 생성자

    public static Payment initiated(String orderId, long amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.INITIATED;
        return payment;
    }
}

// 사용
Payment payment = Payment.initiated("ORDER-123", 10000);
```

#### 3. Optional 활용
```java
// ✅ Good
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

// 사용
User user = findByEmail(email)
    .orElseThrow(() -> new UserNotFoundException(email));

// ❌ Bad: null 반환
public User findByEmail(String email) {
    return userRepository.findByEmail(email);  // null 가능
}
```

#### 4. Exception 처리
```java
// Domain Exception: 비즈니스 예외
public class PaymentAlreadyConfirmedException extends DomainException {
    public PaymentAlreadyConfirmedException(String orderId) {
        super("PAYMENT_ALREADY_CONFIRMED",
              "Payment already confirmed: " + orderId);
    }
}

// 사용
if (payment.isConfirmed()) {
    throw new PaymentAlreadyConfirmedException(payment.getOrderId());
}

// GlobalExceptionHandler에서 처리
@ExceptionHandler(PaymentAlreadyConfirmedException.class)
public ResponseEntity<ErrorResponse> handle(PaymentAlreadyConfirmedException ex) {
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(ex));
}
```

### 문서화

#### 1. JavaDoc (공개 API에만)
```java
/**
 * 결제를 승인합니다.
 *
 * @param command 결제 승인 커맨드 (orderId, paymentKey, amount 포함)
 * @return 승인된 결제 정보
 * @throws PaymentNotFoundException 결제를 찾을 수 없는 경우
 * @throws PaymentAmountMismatchException 금액이 일치하지 않는 경우
 */
public ConfirmPaymentResponse confirm(ConfirmPaymentCommand command) {
    // ...
}
```

#### 2. 코드 주석 (복잡한 비즈니스 로직에만)
```java
// ✅ Good: 왜(Why)를 설명
// Toss Payments는 동일한 orderId로 중복 승인 시도 시 409 에러 반환
// 멱등성 보장을 위해 먼저 승인 여부 체크
if (payment.isConfirmed()) {
    return ConfirmPaymentResponse.from(payment);
}

// ❌ Bad: 무엇을(What) 반복
// 결제가 승인되었는지 체크
if (payment.isConfirmed()) {
    return ConfirmPaymentResponse.from(payment);
}
```

## 데이터베이스 가이드

### JPA Entity 작성 규칙

#### 1. Entity vs Domain Model 분리
```java
// JPA Entity (Infrastructure Layer)
@Entity
@Table(name = "payment")
class PaymentJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String orderId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Version  // 낙관적 잠금
    private Long version;
}

// Domain Model (Domain Layer)
public class Payment {
    private Long id;
    private String orderId;
    private long amount;
    private PaymentStatus status;
    // 순수 비즈니스 로직
}

// Mapper (Infrastructure Layer)
@Mapper(componentModel = "spring")
interface PaymentMapper {
    Payment toDomain(PaymentJpaEntity entity);
    PaymentJpaEntity toEntity(Payment domain);
}
```

#### 2. 낙관적 잠금 사용
```java
@Entity
class PaymentJpaEntity {
    @Version
    private Long version;  // 동시성 제어
}

// 사용 시 OptimisticLockException 처리
try {
    paymentRepository.save(payment);
} catch (OptimisticLockException e) {
    throw new PaymentConcurrentModificationException();
}
```

#### 3. Auditing
```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

// 사용
@Entity
class PaymentJpaEntity extends BaseEntity {
    // createdAt, updatedAt 자동 관리
}
```

### 쿼리 최적화

#### 1. N+1 문제 방지
```java
// ❌ Bad: N+1 발생
List<Inquiry> inquiries = inquiryRepository.findAll();
for (Inquiry inquiry : inquiries) {
    System.out.println(inquiry.getUser().getName());  // 각각 쿼리 발생
}

// ✅ Good: Fetch Join
@Query("SELECT i FROM Inquiry i JOIN FETCH i.user WHERE i.answered = false")
List<Inquiry> findUnansweredWithUser();
```

#### 2. Pagination
```java
public Page<Inquiry> findInquiries(Pageable pageable) {
    return inquiryRepository.findAll(pageable);
}

// 사용
Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
Page<Inquiry> page = findInquiries(pageable);
```

## 보안 가이드

### JWT 토큰 관리

#### 1. 토큰 발급
```java
public TokenPair issueTokens(UserId userId, Set<Role> roles) {
    String accessToken = jwtProvider.generateAccessToken(userId, roles);
    String refreshToken = jwtProvider.generateRefreshToken(userId);

    // Refresh Token을 Redis에 저장 (TTL 설정)
    tokenRegistry.registerRefreshToken(userId, refreshToken);

    return new TokenPair(accessToken, refreshToken);
}
```

#### 2. 토큰 검증 (JwtAuthenticationFilter)
```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain chain) {
    String token = extractToken(request);

    if (token != null && jwtProvider.validateToken(token)) {
        // 토큰 무효화 체크 (블랙리스트)
        if (tokenRegistry.isRevoked(token)) {
            sendUnauthorized(response, "Token revoked");
            return;
        }

        // SecurityContext에 인증 정보 설정
        JwtPrincipal principal = jwtProvider.getPrincipal(token);
        Authentication auth = new JwtAuthenticationToken(principal);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    chain.doFilter(request, response);
}
```

#### 3. 토큰 갱신
```java
@Transactional
public TokenPair refresh(String refreshToken) {
    // Refresh Token 검증
    if (!jwtProvider.validateToken(refreshToken)) {
        throw new InvalidTokenException();
    }

    JwtPrincipal principal = jwtProvider.getPrincipal(refreshToken);
    UserId userId = principal.getUserId();

    // Redis에서 유효한 Refresh Token인지 확인
    if (!tokenRegistry.isValidRefreshToken(userId, refreshToken)) {
        throw new InvalidRefreshTokenException();
    }

    // 기존 토큰 무효화
    tokenRegistry.revokeRefreshToken(userId, refreshToken);

    // 새로운 토큰 쌍 발급
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    return issueTokens(userId, user.getRoles());
}
```

### 권한 제어

#### 1. Method Security
```java
@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    @PostMapping
    @PreAuthorize("hasRole('USER')")  // USER 역할 필요
    public ResponseEntity<InquiryResponse> create(@RequestBody CreateInquiryRequest request) {
        // ...
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasRole('ADMIN')")  // ADMIN 역할 필요
    public ResponseEntity<Void> answer(@PathVariable Long id) {
        // ...
    }
}
```

#### 2. 소유권 검증
```java
@Service
public class UpdateInquiryService {

    public InquiryResponse update(Long inquiryId, UpdateInquiryCommand command, UserId userId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        // 소유권 검증
        if (!inquiry.isOwnedBy(userId)) {
            throw new InquiryAccessDeniedException();
        }

        inquiry.update(command.name(), command.phone(), command.note());
        return InquiryResponse.from(inquiryRepository.save(inquiry));
    }
}
```

### CORS 설정
```java
@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

## 테스트 가이드

### 테스트 전략

#### 1. 단위 테스트 (Domain Layer)
```java
class PaymentTest {

    @Test
    @DisplayName("결제 승인 후 상태가 CONFIRMED로 변경된다")
    void confirm_changesStatusToConfirmed() {
        // Given
        Payment payment = Payment.initiated("ORDER-123", 10000, UserId.of(1L));
        payment.authorize("test-payment-key");

        // When
        payment.confirm();

        // Then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(payment.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 승인된 결제는 재승인할 수 없다")
    void confirm_throwsException_whenAlreadyConfirmed() {
        // Given
        Payment payment = Payment.initiated("ORDER-123", 10000, UserId.of(1L));
        payment.authorize("test-payment-key");
        payment.confirm();

        // When & Then
        assertThatThrownBy(() -> payment.confirm())
            .isInstanceOf(IllegalStateTransitionException.class)
            .hasMessageContaining("Cannot transition");
    }
}
```

#### 2. 통합 테스트 (Application Layer)
```java
@SpringBootTest
@Transactional
class ConfirmPaymentServiceIntegrationTest {

    @Autowired
    private ConfirmPaymentService confirmPaymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private TossPaymentsPort tossPaymentsPort;

    @Test
    @DisplayName("결제 승인 성공 시 결제 상태가 CONFIRMED로 변경된다")
    void confirm_updatesPaymentStatus() {
        // Given
        Payment payment = Payment.initiated("ORDER-123", 10000, UserId.of(1L));
        payment.authorize("test-payment-key");
        paymentRepository.save(payment);

        TossConfirmResponse mockResponse = TossConfirmResponse.builder()
            .orderId("ORDER-123")
            .paymentKey("test-payment-key")
            .approvedAt("2024-01-01T00:00:00")
            .build();

        when(tossPaymentsPort.confirm(any())).thenReturn(mockResponse);

        ConfirmPaymentCommand command = new ConfirmPaymentCommand(
            "ORDER-123",
            "test-payment-key",
            10000
        );

        // When
        ConfirmPaymentResponse response = confirmPaymentService.confirm(command);

        // Then
        assertThat(response.status()).isEqualTo(PaymentStatus.CONFIRMED);

        Payment updatedPayment = paymentRepository.findByOrderId("ORDER-123").get();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }
}
```

#### 3. API 테스트 (Infrastructure Layer)
```java
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfirmPaymentService confirmPaymentService;

    @Test
    @DisplayName("POST /api/payments/confirm - 결제 승인 성공")
    @WithMockUser(roles = "USER")
    void confirmPayment_returnsOk() throws Exception {
        // Given
        ConfirmPaymentResponse mockResponse = new ConfirmPaymentResponse(
            "ORDER-123",
            "test-payment-key",
            10000,
            PaymentStatus.CONFIRMED
        );

        when(confirmPaymentService.confirm(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/payments/confirm")
                .header("Idempotency-Key", "test-idempotency-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "ORDER-123",
                        "paymentKey": "test-payment-key",
                        "amount": 10000
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value("ORDER-123"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
```

### 테스트 데이터 관리

#### 1. Test Fixtures
```java
public class PaymentFixtures {

    public static Payment initiatedPayment() {
        return Payment.initiated("ORDER-TEST-001", 10000, UserId.of(1L));
    }

    public static Payment confirmedPayment() {
        Payment payment = initiatedPayment();
        payment.authorize("test-payment-key");
        payment.confirm();
        return payment;
    }
}

// 사용
@Test
void test() {
    Payment payment = PaymentFixtures.confirmedPayment();
    // ...
}
```

#### 2. TestContainers (선택사항)
```java
@Testcontainers
@SpringBootTest
class PaymentRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    // 테스트 코드...
}
```

## API 개발 가이드

### API 응답 형식

#### 1. 성공 응답
```json
{
  "orderId": "ORDER-123",
  "paymentKey": "test-payment-key",
  "amount": 10000,
  "status": "CONFIRMED",
  "approvedAt": "2024-01-01T00:00:00Z"
}
```

#### 2. 에러 응답
```json
{
  "errorCode": "PAYMENT_NOT_FOUND",
  "message": "Payment not found with orderId: ORDER-123",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/payments/confirm"
}
```

### OpenAPI/Swagger 문서화

```java
@Operation(
    summary = "결제 승인",
    description = "클라이언트에서 인증된 결제를 서버에서 최종 승인합니다."
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "결제 승인 성공",
        content = @Content(schema = @Schema(implementation = ConfirmPaymentResponse.class))
    ),
    @ApiResponse(
        responseCode = "404",
        description = "결제를 찾을 수 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
        responseCode = "409",
        description = "이미 승인된 결제",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
})
@PostMapping("/confirm")
public ResponseEntity<ConfirmPaymentResponse> confirm(
    @Parameter(description = "멱등성 키", required = true)
    @RequestHeader("Idempotency-Key") String idempotencyKey,

    @Parameter(description = "결제 승인 요청", required = true)
    @RequestBody @Valid ConfirmPaymentRequest request
) {
    // ...
}
```

## 배포 가이드

### Docker 이미지 빌드

```bash
# 1. JAR 빌드
./gradlew clean bootJar

# 2. Docker 이미지 빌드
docker build -t ecozone-api:1.0.0 .

# 3. 이미지 확인
docker images | grep ecozone-api
```

### Docker Compose 배포

```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ecozone
    volumes:
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"

  api:
    image: ecozone-api:1.0.0
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ecozone
      SPRING_REDIS_HOST: redis
    ports:
      - "8080:8080"
    volumes:
      - /srv/app/application.yml:/srv/app/application.yml:ro

volumes:
  mysql-data:
  redis-data:
```

### 환경별 설정

#### application-dev.yml
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    co.ecozone.ecozoneapi: DEBUG
```

#### application-prod.yml
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    co.ecozone.ecozoneapi: INFO
```

## 문제 해결

### 자주 발생하는 이슈

#### 1. OptimisticLockException
**증상**: 동시에 같은 결제를 수정하려고 할 때 발생

**해결**:
```java
@Retryable(
    value = OptimisticLockException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
public ConfirmPaymentResponse confirm(ConfirmPaymentCommand command) {
    // ...
}
```

#### 2. JWT Token 만료
**증상**: 401 Unauthorized 응답

**해결**: Refresh Token으로 새로운 Access Token 발급
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Authorization: Bearer <refresh-token>"
```

#### 3. CORS 에러
**증상**: 브라우저에서 "CORS policy" 에러

**해결**: application.yml에 허용할 Origin 추가
```yaml
cors:
  allowed-origins:
    - http://localhost:3000
    - https://yourdomain.com
```

#### 4. Redis 연결 실패
**증상**: "Unable to connect to Redis" 에러

**해결**:
```bash
# Redis 상태 확인
docker-compose ps redis

# Redis 재시작
docker-compose restart redis

# Redis 로그 확인
docker-compose logs redis
```

### 디버깅 팁

#### 1. 로깅 레벨 조정
```yaml
logging:
  level:
    co.ecozone.ecozoneapi: DEBUG
    org.springframework.security: TRACE
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 2. Actuator 활용
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# 메트릭 확인
curl http://localhost:8080/actuator/metrics
```

## 참고 자료

### 내부 문서
- [API 명세서](http://localhost:8080/swagger-ui.html)
- [아키텍처 결정 기록 (ADR)](./docs/adr/)

### 외부 자료
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Toss Payments API](https://docs.tosspayments.com/reference)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)

---

문의사항이나 개선 제안은 팀 채널로 연락주세요.
