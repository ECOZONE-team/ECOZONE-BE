# PR 제목
<!-- 무엇을/왜 바꾸는지 한 줄로. 예) auth: 보안 설정 YAML 외부화 및 정책 적용 구조 개선 -->

## 🌱 브랜치
`[이름]/[날짜]`  <!-- 예: feature/auth-policy/2025-08-13 -->

## ➕ 관련 이슈
- Resolves: #이슈
- Related:  #이슈, #이슈

---

## ✅ 요약 (TL;DR)
- 
-
-

## 📋 상세 설명
### 범위(Scope) / 비범위(Out of Scope)
- 포함:
- 제외:

### 주요 변경사항
- [백엔드]
- [보안/Auth]
- [설정/환경]
- [서드파티/인프라]

### API / 라우팅 변경
- `METHOD /path` — 목적/권한
- 요청/응답 스키마 변경
- 마이그레이션/클라이언트 영향

### 설정 / 시크릿
- 추가/변경 키: `JWT_SECRET_BASE64`, `FRONTEND_ORIGIN`, ...
- 주입: ENV / Secret Manager / `--spring.config.additional-location`
- 보안: `application.yml` 등 **레포 미추적**, `application-example.yml`만 커밋

---

## 📸 스크린샷 / 이미지 (선택)
| 구분   | 캡처 |
|-------|-----|
| Before |     |
| After  |     |

---

## 🧪 테스트 계획 (How to Test)
### 자동 테스트
- [ ] 단위(Unit): 도메인/유스케이스 테스트 통과
- [ ] 통합(Integration): 보안 필터/예외 핸들러/CORS 검증
- [ ] 커버리지 목표: Line ≥ __%, Branch ≥ __%

### 수동 테스트 시나리오
1) 공개 엔드포인트 → 200
2) 보호 엔드포인트 **무토큰** → 401(JSON: `code`, `message`)
3) **잘못된 토큰**(BAD_SIGNATURE/EXPIRED/NOT_BEFORE) → 401 사유 확인
4) 권한 부족(ROLE 미일치) → 403(JSON)
5) **CORS 프리플라이트(OPTIONS)** → 허용 Origin/Methods/Headers 확인

<details>
<summary>로컬 실행 가이드</summary>

~~~bash
cp src/main/resources/application-example.yml ./config/application.yml
export JWT_SECRET_BASE64=...
export FRONTEND_ORIGIN=https://localhost:3000
./gradlew bootRun --args='--spring.config.additional-location=optional:./config/'
~~~
</details>

---

## ✅ 수용 기준 (Acceptance Criteria)
- [ ] API가 명세대로 동작
- [ ] 인증/인가 정책 일치(permitAll/authenticated/roleBased)
- [ ] 설정 외부화로 코드 하드코딩 없음
- [ ] 회귀 이슈 없음

---

## 🔒 보안 체크리스트
- [ ] 최소 권한/화이트리스트 적용
- [ ] CORS 허용 범위 최소화
- [ ] 민감정보(토큰/시크릿/PII) 로그 미노출
- [ ] JWT 키 길이/알고리즘 정책 준수(예: HS256 ≥ 256bit)

---

## 🧩 API 계약
- [ ] Swagger/OpenAPI 갱신
- [ ] 에러 포맷(JSON: `timestamp`, `status`, `error`, `code`, `message`, `path`) 일관성 유지
- [ ] 클라이언트 영향/버전 정책 문서화

---

## 🗃️ DB/마이그레이션
- [ ] 스키마 변경 없음
- [ ] 스키마 변경 있음 → Flyway/Liquibase 스크립트 포함 + 롤백 경로

---

## 📈 성능 & 관측
- [ ] 성능 영향 없음 / 측정 결과 첨부
- [ ] 로그 레벨/메시지 표준화
- [ ] 필요한 메트릭/트레이스 포인트 추가

---

## 🚀 배포 계획 & 🧯 롤백
**배포**: 단계/타이밍/Feature Flag  
**롤백**: 조건/방법(커밋/태그/DB 롤백 스크립트)

---

## ⏭️ 리뷰 후 다음 단계 (Next)
- 
-
-

---

## 👥 리뷰어 / 승인
- Reviewer: @__
- Approver: @__

---

## 🧾 PR 유형
- [ ] 새로운 기능 추가 (feat)
- [ ] 버그 수정 (fix)
- [ ] UI/CSS 변경
- [ ] 비기능 변경(오타/탭/변수명 등)
- [ ] 리팩토링 (refactor)
- [ ] 주석 추가/수정 (docs-in-code)
- [ ] 문서 수정 (docs)
- [ ] 테스트 추가/수정 (test)
- [ ] 빌드/패키지/CI 변경 (build/ci)
- [ ] 파일/폴더 명 변경 (chore)
- [ ] 파일/폴더 삭제 (chore)

---

## ✅ PR 체크리스트
- [ ] 커밋 메시지 **Conventional Commits** 준수
- [ ] 모든 테스트 통과(CI 포함)
- [ ] 린트/포맷팅 통과
- [ ] 문서/README 업데이트
- [ ] 영향 팀에 변경사항 공유  
