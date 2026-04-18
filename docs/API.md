# ReCheck API 명세서

> **Base URL**: `https://api.reajoucheck.site` 
> **Content-Type**: `application/json` (파일 업로드 제외)
> **인증**: `Authorization: Bearer {accessToken}` 헤더 사용

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
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**에러 케이스**
| 상태 | 메시지 |
|------|--------|
| `401` | 아이디 또는 비밀번호가 올바르지 않습니다. |

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

### 영수증 이미지 업로드

**POST** `/api/receipts/upload` `🔒 인증 필요`

> `Content-Type: multipart/form-data`

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

> 모든 관리자 API는 `🔒 관리자 계정 인증 필요`

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

**GET** `/api/admin/weeks/current`

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