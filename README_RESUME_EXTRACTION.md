# 자소서 추출 기능 (Resume Extraction)

## 개요
이 기능은 PDF, DOCX, TXT 파일에서 텍스트를 추출하고, OpenAI GPT API를 활용하여 자소서 내용만을 정제하여 추출하는 기능입니다.

## 주요 기능

### 1. 파일 텍스트 추출
- **지원 파일 형식**: PDF, DOCX, TXT
- **사용 라이브러리**: 
  - PDF: Apache PDFBox
  - DOCX: Apache POI
  - TXT: 기본 텍스트 처리

### 2. GPT 기반 자소서 내용 추출
- OpenAI GPT-3.5-turbo 모델 사용
- 생 텍스트에서 자소서 관련 내용만 추출
- 불필요한 개인정보, 서식 정보 제거

## API 엔드포인트

### 1. 자소서 생성 및 저장
```http
POST /api/resume/file
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (PDF, DOCX, TXT)
- userEmail: String (사용자 이메일)
- resumeTitle: String (선택사항, 자소서 제목)
```

**응답 예시:**
```json
{
  "success": true,
  "message": "자소서가 성공적으로 생성되었습니다.",
  "data": {
    "resumeId": 1,
    "userEmail": "user@example.com",
    "resumeTitle": "업로드된 자소서",
    "resumeText": "정제된 자소서 내용...",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 2. 자소서 텍스트만 추출 (저장하지 않음)
```http
POST /api/resume/extract
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (PDF, DOCX, TXT)
```

**응답 예시:**
```json
{
  "success": true,
  "message": "자소서 텍스트 추출 완료",
  "data": "GPT로 정제된 자소서 내용..."
}
```

### 3. 생 텍스트 추출 (GPT 처리 없음)
```http
POST /api/resume/raw-text
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (PDF, DOCX, TXT)
```

**응답 예시:**
```json
{
  "success": true,
  "message": "원본 텍스트 추출 완료",
  "data": "파일에서 추출된 원본 텍스트..."
}
```

## 설정 방법

### 1. OpenAI API 키 발급
1. [OpenAI 플랫폼](https://platform.openai.com/)에서 API 키 발급
2. 생성된 키를 안전한 곳에 저장 (다시 볼 수 없음)

### 2. API 키 설정 (보안 중요!)

#### 방법 1: 환경 변수 사용 (권장)
```bash
# Windows PowerShell
$env:OPENAI_API_KEY="sk-your-actual-openai-api-key-here"

# macOS/Linux
export OPENAI_API_KEY="sk-your-actual-openai-api-key-here"
```

#### 방법 2: 로컬 설정 파일 사용
`application-local.properties` 파일에 실제 API 키 입력:
```properties
openai.api.key=sk-your-actual-openai-api-key-here
```

그리고 프로파일 활성화:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. application.properties 확인
```properties
# 환경 변수 사용 (기본값 포함)
openai.api.key=${OPENAI_API_KEY:your-openai-api-key-here}
openai.api.url=https://api.openai.com/v1/chat/completions

# 파일 업로드 설정
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
```

### 4. Git 보안 설정
`.gitignore` 파일에 다음이 포함되어 있는지 확인:
```
.env
.env.local
application-local.properties
application-dev.properties
application-prod.properties
**/application-secret.properties
```

**⚠️ 중요**: 실제 API 키를 Git에 커밋하지 마세요! 자세한 보안 설정은 `ENVIRONMENT_SETUP.md` 파일을 참조하세요.

## GPT 프롬프트 전략

### 추출 기준
- **포함할 내용**: 지원동기, 성장과정, 성격의 장단점, 입사 후 포부, 개인 경험 및 역량
- **제외할 내용**: 개인정보, 단순 스펙 나열, 문서 서식, 불필요한 기호

### 프롬프트 특징
- `temperature: 0.3` (일관성 있는 추출을 위해 낮은 값 사용)
- `max_tokens: 2000` (충분한 텍스트 길이 확보)
- 구조화된 프롬프트로 정확한 추출 유도

## 사용 예시

### cURL을 사용한 테스트
```bash
# 자소서 생성 및 저장
curl -X POST http://localhost:9000/api/resume/file \
  -F "file=@resume.pdf" \
  -F "userEmail=test@example.com" \
  -F "resumeTitle=삼성전자 지원 자소서"

# 자소서 텍스트만 추출
curl -X POST http://localhost:9000/api/resume/extract \
  -F "file=@resume.pdf"

# 생 텍스트 추출
curl -X POST http://localhost:9000/api/resume/raw-text \
  -F "file=@resume.pdf"
```

### JavaScript (프론트엔드)
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('userEmail', 'user@example.com');
formData.append('resumeTitle', '자소서 제목');

fetch('/api/resume/file', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('자소서 생성 성공:', data.data);
  } else {
    console.error('오류:', data.message);
  }
});
```

## 오류 처리

### 일반적인 오류 상황
1. **파일이 비어있음**: HTTP 400 Bad Request
2. **지원하지 않는 파일 형식**: RuntimeException
3. **사용자를 찾을 수 없음**: RuntimeException
4. **GPT API 호출 실패**: GPT 서비스에서 오류 메시지 반환
5. **자소서 내용 없음**: "업로드된 파일에서 자소서 내용을 찾을 수 없습니다."

### 오류 응답 형식
```json
{
  "success": false,
  "message": "오류 메시지",
  "data": null
}
```

## 주의사항

1. **API 키 보안**: 실제 운영 환경에서는 환경 변수나 안전한 설정 관리 도구 사용
2. **파일 크기 제한**: 현재 10MB로 설정됨
3. **GPT API 비용**: 토큰 사용량에 따른 비용 발생
4. **처리 시간**: GPT API 호출로 인한 응답 지연 가능성
5. **한국어 처리**: GPT 모델의 한국어 처리 성능에 의존

## 확장 가능성

- **다양한 파일 형식 지원**: HWP, RTF 등
- **GPT 모델 업그레이드**: GPT-4 등 더 성능 좋은 모델 사용
- **배치 처리**: 여러 파일 동시 처리
- **캐싱**: 동일한 파일에 대한 중복 처리 방지
- **템플릿 기반 추출**: 회사별, 직무별 맞춤 추출 