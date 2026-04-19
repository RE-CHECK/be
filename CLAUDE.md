# ReCheck Backend - CLAUDE.md

## 프로젝트 개요

대학교 영수증 인증 기반 커뮤니티 플랫폼의 백엔드 서버.
학생들이 영수증 이미지를 업로드하면 S3에 저장되고, 단과대별 결제 금액 집계 및 참여 현황을 제공한다.

- **메인 브랜치:** `main`
- **개발 브랜치:** `develop`
- **패키지 루트:** `com.be.recheckbe`

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.4 |
| Build Tool | Gradle |
| Database | PostgreSQL |
| ORM | Spring Data JPA (Hibernate) |
| 인증 | JWT (jjwt 0.13.0) + Spring Security |
| 파일 저장 | AWS S3 (sdk 2.25.60) |
| API 문서 | SpringDoc OpenAPI 2.8.1 (Swagger UI) |
| 유틸 | Lombok |

---

## 프로젝트 구조

```
src/main/java/com/be/recheckbe/
├── domain/                     # 비즈니스 도메인 (기능별 패키지)
│   ├── admin/                  # 관리자 기능 (통계, CSV, 주차 관리)
│   ├── auth/                   # 인증/인가 (로그인, 회원가입)
│   ├── college/                # 단과대학 데이터
│   ├── department/             # 학과 데이터
│   ├── receipt/                # 영수증 업로드 및 통계/집계
│   ├── user/                   # 사용자 엔티티 및 대시보드
│   └── week/                   # 주차 활성화 상태 관리
└── global/                     # 전역 공통 모듈
    ├── config/                 # 각종 설정 (CORS, S3, Swagger, Jackson)
    ├── security/               # Spring Security 설정, CustomUserDetails
    ├── jwt/                    # JWT 생성/검증/필터
    ├── exception/              # 글로벌 예외 처리 및 에러 코드
    ├── s3/                     # S3 파일 업로드 서비스
    ├── common/                 # BaseTimeEntity (createdAt, modifiedAt)
    ├── response/               # 공통 응답 포맷 (BaseResponse<T>)
    ├── init/                   # 앱 시작 시 초기 데이터 생성
    └── ocr/                    # Naver OCR 영수증 정보 추출
```

각 도메인 내부는 `controller / service / repository / entity / dto` 레이어로 구성.

---

## 아키텍처

**레이어드 아키텍처 + 도메인 패키지 구조**

```
Controller → Service (Interface + Impl) → Repository → Entity
```

- Controller: `@RestController`, RESTful 엔드포인트
- Service: 인터페이스 + `Impl` 구현 클래스 패턴 (`AuthService` / `AuthServiceImpl`)
- Repository: Spring Data JPA `JpaRepository` 상속
- Entity: JPA 어노테이션 적용, `BaseTimeEntity` 상속으로 감사 필드 공통화

**응답 포맷 통일** — 모든 API는 `BaseResponse<T>` 래퍼 사용:
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... }
}
```

---

## 코드 스타일 및 컨벤션

### 네이밍
- 패키지: `com.be.recheckbe.domain.{feature}.{layer}` (소문자)
- 클래스: PascalCase
- DTO: `{Action}Request`, `{Action}Response` 접미사 (예: `LoginRequest`, `LoginResponse`)
- Service 구현체: `{Name}ServiceImpl` 접미사
- Repository: `{Entity}Repository` 접미사

### Lombok
- `@Getter`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor` 사용
- DI는 `@RequiredArgsConstructor` + `final` 필드 사용 (`@Autowired` 미사용)

### 예외 처리
- `CustomException(ErrorCode)` 패턴으로 통일
- 에러 코드는 도메인별 Enum으로 관리 (`GlobalErrorCode`, `AuthErrorCode`, `ReceiptErrorCode`, `S3ErrorCode`)
- `GlobalExceptionHandler`에서 일괄 처리

### Git 커밋 메시지
이모지 프리픽스 사용:
```
✨ Feat      새 기능
🐛 Fix       버그 수정
♻️ Refactor  리팩토링
📝 Docs      문서
🎨 Style     코드 포맷
✅ Test      테스트
🔧 Chore     빌드/설정
```
- 별도의 수정 사항은 README.md 파일을 참고하여 커밋 메시지 컨벤션을 지킵니다.

### Github Convention
- PR과 Issue 생성은 `.github` 폴더 하위 템플릿 형식에 맞게 작성합니다.
- 코드 수정이 완료되면 `develop` 브랜치로 PR을 생성합니다.
- PR 생성 시 외부에 노출되면 안 되는 정보는 적지 않고 검토사항에 확인을 요망한다고 표시합니다.

---

## 주요 도메인 모델

### User
- `username` (unique), `password` (BCrypt), `name`, `phoneNumber`
- `studentNumber` (Integer, 예: 2023301001 → 23학번), `studentCardImageUrl` (S3 URL)
- `role`: `USER` | `ADMIN`
- `department` (ManyToOne), `receipts` (OneToMany)

### Receipt
- `imageUrl` (S3 URL), `paymentAmount`, `storeName`, `cardCompany`, `confirmNum` (unique)
- `weekNumber` (Integer, null = 테스트 기간, 1~3 = 운영 주차)
- `user` (ManyToOne)

### College / Department
- `College` 1:N `Department` 관계

### Week
- 단일 행 싱글턴 (`id = 1` 고정)
- `weekNumber` (null = 테스트 기간, 1~3 = 활성화된 주차)
- 영수증 업로드 시 현재 활성 주차가 자동으로 기록됨

---

## 보안 설정

### 공개 엔드포인트 (인증 불필요)
- `POST /api/auth/**`
- `GET /api/colleges/**`
- `GET /api/receipts/total-participation`
- `GET /api/receipts/total-all-payment`
- `GET /api/receipts/week2-ranking`
- `GET /api/receipts/week3-challenge`
- `GET /api/admin/weeks/current`
- Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`)

### 인증 필요
- 위 목록 외 모든 엔드포인트 (`Authorization: Bearer {accessToken}`)

### 관리자 전용
- `/api/admin/**` (`ADMIN` role 필요)

### JWT
- Access Token 만료: 1시간
- Refresh Token 만료: 14일
- 알고리즘: HS256
- Authorization 헤더 Bearer 토큰 방식

---

## API 엔드포인트 요약

상세 명세(요청/응답 예시)는 [`docs/API.md`](docs/API.md) 참고.

### Auth (인증) — 공개
```
GET    /api/auth/check-username                   # 아이디 중복 확인
POST   /api/auth/login                            # 로그인
POST   /api/auth/register                         # 회원가입 (multipart - 학생증 이미지 포함)
```

### Colleges (단과대/학과) — 공개
```
GET    /api/colleges                              # 단과대 목록
GET    /api/colleges/{collegeId}/departments      # 학과 목록
```

### Receipts (영수증) — 혼합
```
POST   /api/receipts/analyze                      # 영수증 OCR 분석 (인증 필요, multipart) — S3/DB 저장 없음
POST   /api/receipts/confirm                      # 영수증 업로드 확정 (인증 필요, multipart) — S3 업로드 + DB 저장
GET    /api/receipts/total-participation          # 전체 누적 참여 횟수 (공개)
GET    /api/receipts/total-all-payment            # 전체 누적 소비금액 (공개)
GET    /api/receipts/college-total-payment        # 내 단과대 누적 소비금액 (인증 필요)
GET    /api/receipts/week2-ranking                # 2주차 대진별 단과대 랭킹 (공개)
GET    /api/receipts/week3-challenge              # 3주차 학번 대결 현황 (공개)
```

### Users (사용자) — 인증 필요
```
GET    /api/users/me/dashboard                    # 내 대시보드 (이름, 단과대, 총 소비금액)
```

### Admin (관리자) — 관리자 전용
```
GET    /api/admin/users/stats                     # 가입자 수 통계 (오늘/누적)
GET    /api/admin/users/csv                       # 유저 가입 정보 CSV 다운로드
GET    /api/admin/receipts/csv                    # 일자별 단과대 소비금액 CSV 다운로드
GET    /api/admin/weeks/current                   # 현재 활성화 주차 조회
PATCH  /api/admin/weeks/{weekNumber}/activate     # 특정 주차 활성화 (1~3)
PATCH  /api/admin/weeks/deactivate                # 주차 비활성화 (테스트 기간 전환)
```

---

## 환경 설정

### 로컬 (`application-local.properties`)
- DB: `localhost:5432/recheck` (root/1234)
- JPA ddl-auto: `update`
- SQL 로그 활성화

### 개발서버 (`application-dev.properties`)
- DB: 환경변수로 주입 (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- JPA ddl-auto: `validate`
- SQL 로그 비활성화

### AWS S3
- 버킷: `recheck-bucket`
- 리전: `ap-northeast-2` (서울)
- 업로드 경로: `receipt/`, `studentCard/`
- 허용 파일: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp` (최대 5MB)

---

## 로컬 실행 방법

```bash
# PostgreSQL 실행 후 (DB: recheck, user: root, pw: 1234)
./gradlew bootRun --args='--spring.profiles.active=local'
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## 초기 데이터

앱 시작 시 자동 생성:
- 관리자 계정: `admin` / `1234`
- 단과대 및 학과 기본 데이터
- 주차 설정 초기값 (`weekNumber = null`, 테스트 기간)