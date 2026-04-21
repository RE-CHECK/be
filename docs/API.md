# ReCheck API 명세서

> **Base URL**: `https://api.reajoucheck.site` 
> **Content-Type**: `application/json` (파일 업로드 제외)
> **인증**: `Authorization: Bearer {accessToken}` 헤더 사용
> **Refresh Token**: HttpOnly 쿠키(`refreshToken`)로 관리 — JS 접근 불가, 로그인 시 자동 설정

### Access Token 만료 처리

Access Token이 만료된 상태로 요청을 보내면 서버가 자동으로 재발급합니다.

1. 만료된 Access Token으로 요청
2. 서버가 DB에 저장된 Refresh Token을 검증
3. **Refresh Token 유효** → 새 Access Token을 응답 헤더 `Authorization`에 담아 반환, 요청 정상 처리
4. **Refresh Token 만료** → `401` 반환 → 재로그인 필요

클라이언트는 모든 응답에서 `Authorization` 헤더를 확인하여 새 토큰이 있으면 갱신해야 합니다.

---

## 공통 응답 형식

모든 API는 아래 형식으로 응답합니다.

```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { }
}
```

### 에러 응답

```json
{
  "success": false,
  "code": 401,
  "message": "에러 메시지",
  "data": null
}
```

| HTTP 상태 | 상황 |
|-----------|------|
| `400` | 요청값 검증 실패 |
| `401` | 인증 토큰 없음 / 만료 / 유효하지 않음 |
| `404` | 리소스 없음 |
| `409` | 중복 데이터 |
| `500` | 서버 오류 |

---

## 인증 (Auth)

### 로그인

**POST** `/api/auth/login`

**Request Body**
```json
{
  "username": "user123",
  "password": "password123"
}
```

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 2,
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

> Refresh Token은 응답 body에 포함되지 않으며, `Set-Cookie: refreshToken=...; HttpOnly; Secure; SameSite=Strict` 헤더로 자동 설정됩니다.

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `401` | 아이디 또는 비밀번호가 올바르지 않습니다. |

---

### 로그아웃

**POST** `/api/auth/logout` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": null
}
```

> DB에 저장된 Refresh Token을 삭제하고 쿠키를 만료시킵니다.

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `401` | 로그인이 필요합니다. |

---

### 회원가입

**POST** `/api/auth/register`

> `Content-Type: multipart/form-data`

**Request (multipart/form-data)**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `request` | JSON (application/json part) | O | 아래 JSON 형식 |
| `studentCardImage` | File | O | 학생증 이미지 (.jpg, .jpeg, .png, .gif, .webp / 최대 5MB) |

`request` JSON 형식:
```json
{
  "username": "user123",
  "password": "password123",
  "passwordConfirm": "password123",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "studentNumber": 2023301001,
  "departmentId": 3
}
```

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": null
}
```

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `400` | 비밀번호가 일치하지 않습니다. |
| `409` | 이미 사용 중인 아이디입니다. |

---

### 아이디 중복 확인

**GET** `/api/auth/check-username?username={username}`

**Query Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `username` | String | O | 중복 확인할 아이디 |

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": null
}
```

> 사용 가능한 아이디면 `200`, 중복이면 `409` 반환

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `409` | 이미 사용 중인 아이디입니다. |

---

## 단과대 / 학과 (Colleges)

### 단과대 목록 조회

**GET** `/api/colleges`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    { "id": 1, "name": "공과대학" },
    { "id": 2, "name": "소프트웨어융합대학" },
    { "id": 3, "name": "경영대학" }
  ]
}
```

---

### 학과 목록 조회

**GET** `/api/colleges/{collegeId}/departments`

**Path Parameter**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `collegeId` | Long | 단과대 ID |

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    { "id": 1, "name": "컴퓨터공학과" },
    { "id": 2, "name": "전자공학과" }
  ]
}
```

---

## 영수증 (Receipts)

### 영수증 OCR 분석

**POST** `/api/receipts/analyze` `🔒 인증 필요`

> `Content-Type: multipart/form-data`

영수증 이미지를 OCR로 분석하여 결제 정보를 반환합니다. S3 업로드 및 DB 저장은 하지 않습니다.
사용자가 OCR 결과를 확인한 뒤 `/confirm`을 호출해야 최종 저장됩니다.

**Request (multipart/form-data)**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `image` | File | O | 영수증 이미지 (.jpg, .jpeg, .png, .gif, .webp / 최대 5MB) |

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "storeName": "사랑집4",
    "paymentAmount": 15000,
    "cardCompany": "국민카드",
    "confirmNum": "12345678"
  }
}
```

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `400` | 지원하지 않는 카드사 입니다. (국민카드만 허용) |
| `409` | 이미 등록된 영수증입니다. |

---

### 영수증 업로드 확정

**POST** `/api/receipts/confirm` `🔒 인증 필요`

> `Content-Type: multipart/form-data`

OCR 분석 결과를 사용자가 확인한 후 호출합니다. 이미지를 S3에 업로드하고 DB에 저장합니다.

**Request (multipart/form-data)**

| 필드 | 타입 | Content-Type | 필수 | 설명 |
|------|------|--------------|------|------|
| `image` | File | - | O | 영수증 이미지 (.jpg, .jpeg, .png, .gif, .webp / 최대 5MB) |
| `data` | JSON | `application/json` | O | `/analyze` 응답의 OCR 결과 |

`data` JSON 형식:
```json
{
  "storeName": "사랑집4",
  "paymentAmount": 15000,
  "cardCompany": "국민카드",
  "confirmNum": "12345678"
}
```

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "imageUrl": "https://recheck-bucket.s3.ap-northeast-2.amazonaws.com/receipt/...",
    "storeName": "사랑집4",
    "paymentAmount": 15000,
    "cardCompany": "국민카드",
    "confirmNum": "12345678"
  }
}
```

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `400` | 지원하지 않는 카드사 입니다. (국민카드만 허용) |
| `409` | 이미 등록된 영수증입니다. |

---

### 전체 누적 참여 횟수 조회

**GET** `/api/receipts/total-participation`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "totalParticipationCount": 342
  }
}
```

---

### 전체 누적 소비금액 조회

**GET** `/api/receipts/total-all-payment`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "totalAllPaymentAmount": 5120000
  }
}
```

---

### 내 단과대 누적 소비금액 조회

**GET** `/api/receipts/college-total-payment` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "collegeId": 1,
    "collegeName": "공과대학",
    "totalPaymentAmount": 980000
  }
}
```

---

### 2주차 대진별 랭킹 조회

**GET** `/api/receipts/week2-ranking`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "storeName": "사랑집1",
      "rankings": [
        { "rank": 1, "collegeName": "소프트웨어융합대학", "totalPaymentAmount": 320000 },
        { "rank": 2, "collegeName": "공과대학", "totalPaymentAmount": 210000 },
        { "rank": 3, "collegeName": "첨단바이오융합대학", "totalPaymentAmount": 180000 },
        { "rank": 4, "collegeName": "인문대학", "totalPaymentAmount": 90000 }
      ]
    },
    {
      "storeName": "사랑집3",
      "rankings": [
        { "rank": 1, "collegeName": "사회과학대학", "totalPaymentAmount": 270000 },
        { "rank": 2, "collegeName": "국방디지털융합학과", "totalPaymentAmount": 150000 }
      ]
    },
    {
      "storeName": "사랑집2",
      "rankings": [
        { "rank": 1, "collegeName": "경영대학", "totalPaymentAmount": 400000 },
        { "rank": 2, "collegeName": "메디컬", "totalPaymentAmount": 310000 }
      ]
    }
  ]
}
```

---

### 3주차 학번 대결 현황 조회

**GET** `/api/receipts/week3-challenge`

**Response (승패 있음)**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "matchup": "23학번vs24학번",
      "win": "23학번",
      "lose": "24학번",
      "isDraw": false,
      "year1Total": 150000,
      "year2Total": 120000
    },
    {
      "matchup": "25학번vs26학번",
      "win": "26학번",
      "lose": "25학번",
      "isDraw": false,
      "year1Total": 80000,
      "year2Total": 95000
    }
  ]
}
```

**Response (무승부)**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "matchup": "23학번vs24학번",
      "win": null,
      "lose": null,
      "isDraw": true,
      "year1Total": 100000,
      "year2Total": 100000
    }
  ]
}
```

> - `year1Total`: 첫 번째 학번 그룹(23학번 / 25학번) 합산 금액
> - `year2Total`: 두 번째 학번 그룹(24학번 / 26학번) 합산 금액
> - 무승부 시 `win`, `lose`는 `null`

---

## 사용자 (Users)

### 내 대시보드 조회

**GET** `/api/users/me/dashboard` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "name": "홍길동",
    "collegeName": "공과대학",
    "totalPaymentAmount": 45000
  }
}
```

---

## 관리자 (Admin)

> `GET /api/admin/weeks/current`를 제외한 모든 관리자 API는 `🔒 관리자 계정 인증 필요`

### 가입자 수 통계 조회

**GET** `/api/admin/users/stats`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "todayCount": 5,
    "totalCount": 120
  }
}
```

---

### 유저 가입 정보 CSV 다운로드

**GET** `/api/admin/users/csv`

> 파일 다운로드 응답 (`Content-Type: text/csv`)
> 컬럼: `가입일시, 유저ID, 단과대, 학과`

---

### 단과대별 소비금액 CSV 다운로드

**GET** `/api/admin/receipts/csv`

> 파일 다운로드 응답 (`Content-Type: text/csv`)
> 컬럼: `일자, 단과대명, 소비금액합계`

---

### 현재 활성화 주차 조회

**GET** `/api/admin/weeks/current` `🌐 공개`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "weekNumber": 3
  }
}
```

> `weekNumber`가 `null`이면 테스트 기간

---

### 주차 활성화

**PATCH** `/api/admin/weeks/{weekNumber}/activate`

**Path Parameter**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `weekNumber` | int | 활성화할 주차 (1, 2, 3) |

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "weekNumber": 2
  }
}
```

---

### 주차 비활성화

**PATCH** `/api/admin/weeks/deactivate`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "weekNumber": null
  }
}
```

---

### 팝업 등록/수정

**PATCH** `/api/admin/popup` `🔒 관리자 계정 인증 필요`

**Request Body**
```json
{
  "content": "이번 주 이벤트 참여하세요!"
}
```

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "active": true,
    "content": "이번 주 이벤트 참여하세요!",
    "updatedAt": "2026-04-20T10:00:00"
  }
}
```

> 등록 즉시 활성화됩니다. 기존 팝업이 있으면 덮어씁니다.

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `400` | 팝업 내용을 입력해주세요. |

---

### 팝업 비활성화

**PATCH** `/api/admin/popup/deactivate` `🔒 관리자 계정 인증 필요`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "active": false,
    "content": "이번 주 이벤트 참여하세요!",
    "updatedAt": "2026-04-20T10:05:00"
  }
}
```

---

## 팝업 (Popup)

### 팝업 조회

**GET** `/api/popup` `🌐 공개`

**Response**
```json
{
  "success": true,
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "active": true,
    "content": "이번 주 이벤트 참여하세요!",
    "updatedAt": "2026-04-20T10:00:00"
  }
}
```

> - `active: false`이면 팝업을 표시하지 않습니다.
> - 클라이언트는 `updatedAt`을 `localStorage`에 저장하여, 동일한 값이면 팝업을 재표시하지 않습니다. 관리자가 팝업을 새로 등록하면 `updatedAt`이 변경되어 다시 표시됩니다.