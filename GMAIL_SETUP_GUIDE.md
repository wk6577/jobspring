# Gmail SMTP 설정 가이드

## 1. Gmail 2단계 인증 활성화

1. Gmail 계정에 로그인
2. [Google 계정 보안 설정](https://myaccount.google.com/security)으로 이동
3. "2단계 인증" 클릭
4. "시작하기" 클릭하여 2단계 인증 활성화

## 2. 앱 비밀번호 생성

1. [Google 계정 보안 설정](https://myaccount.google.com/security)에서 "앱 비밀번호" 클릭
2. "앱 선택" 드롭다운에서 "메일" 선택
3. "기기 선택" 드롭다운에서 "기타(맞춤 이름)" 선택
4. 이름 입력 (예: "JobAyong Email Service")
5. "생성" 클릭
6. 생성된 16자리 앱 비밀번호를 복사

## 3. application-local.properties 설정

`src/main/resources/application-local.properties` 파일을 생성하고 다음 내용을 추가:

```properties
# Gmail SMTP 인증 정보
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-digit-app-password

# 이메일 발송자 정보
app.email.from=your-email@gmail.com
app.email.from-name=JobAyong
```

## 4. 설정 변경사항

- `your-email@gmail.com`: 실제 Gmail 주소로 변경
- `your-16-digit-app-password`: 2단계에서 생성한 16자리 앱 비밀번호로 변경

## 5. 보안 주의사항

✅ **application-local.properties는 .gitignore에 포함되어 Git에 커밋되지 않습니다**
✅ **민감한 정보(이메일, 비밀번호)는 application-local.properties에서만 관리**
✅ **application.properties에는 기본 설정만 포함**

## 6. 테스트

1. Spring Boot 애플리케이션 재시작
2. 비밀번호 재설정 요청 테스트
3. Gmail 받은편지함에서 이메일 확인

## 주의사항

- 일반 Gmail 비밀번호가 아닌 **앱 비밀번호**를 사용해야 합니다
- 하루 500통의 이메일 발송 제한이 있습니다
- 개발/테스트용으로는 충분하지만, 프로덕션에서는 전문 이메일 서비스 사용을 권장합니다

## 문제 해결

### "Authentication failed" 오류
- 2단계 인증이 활성화되어 있는지 확인
- 앱 비밀번호를 올바르게 입력했는지 확인

### "Connection timeout" 오류
- 방화벽에서 587 포트가 차단되지 않았는지 확인
- 네트워크 연결 상태 확인

### "Could not connect to SMTP host" 오류
- application-local.properties 파일이 올바른 위치에 있는지 확인
- 파일명이 정확한지 확인 (application-local.properties) 