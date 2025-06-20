# 환경 설정 가이드

## API 키 보안 관리

이 프로젝트는 OpenAI API를 사용하므로 API 키를 안전하게 관리해야 합니다.

## 방법 1: 환경 변수 사용 (권장)

### Windows (PowerShell)
```powershell
# 임시 설정 (현재 세션에만 적용)
$env:OPENAI_API_KEY="sk-your-actual-openai-api-key-here"

# 영구 설정 (시스템 환경 변수)
[Environment]::SetEnvironmentVariable("OPENAI_API_KEY", "sk-your-actual-openai-api-key-here", "User")
```

### Windows (Command Prompt)
```cmd
# 임시 설정
set OPENAI_API_KEY=sk-your-actual-openai-api-key-here

# 영구 설정은 시스템 속성 > 고급 > 환경 변수에서 설정
```

### macOS/Linux
```bash
# 임시 설정
export OPENAI_API_KEY="sk-your-actual-openai-api-key-here"

# 영구 설정 (.bashrc 또는 .zshrc에 추가)
echo 'export OPENAI_API_KEY="sk-your-actual-openai-api-key-here"' >> ~/.bashrc
source ~/.bashrc
```

### IDE 설정

#### IntelliJ IDEA
1. Run Configuration 설정
2. Environment variables에 `OPENAI_API_KEY=sk-your-key` 추가

#### VS Code
1. `.vscode/launch.json` 파일 생성
2. 환경 변수 설정:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "name": "Spring Boot",
            "type": "java",
            "request": "launch",
            "mainClass": "com.JobAyong.JobAyongApplication",
            "env": {
                "OPENAI_API_KEY": "sk-your-actual-openai-api-key-here"
            }
        }
    ]
}
```

## 방법 2: 프로파일별 설정 파일 사용

### 1. 로컬 개발용 설정 파일 생성
`src/main/resources/application-local.properties` 파일에 실제 API 키 입력:
```properties
openai.api.key=sk-your-actual-openai-api-key-here
```

### 2. 애플리케이션 실행 시 프로파일 지정
```bash
# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Java 직접 실행
java -jar target/JobAyong-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### 3. IDE에서 프로파일 설정
- IntelliJ: Run Configuration > VM options에 `-Dspring.profiles.active=local` 추가
- VS Code: launch.json에 `"args": ["--spring.profiles.active=local"]` 추가

## 방법 3: .env 파일 사용 (추가 라이브러리 필요)

### 1. 의존성 추가 (pom.xml)
```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>2.5.4</version>
</dependency>
```

### 2. .env 파일 생성 (프로젝트 루트)
```
OPENAI_API_KEY=sk-your-actual-openai-api-key-here
```

### 3. application.properties 수정
```properties
openai.api.key=${OPENAI_API_KEY}
```

## 운영 환경 설정

### Docker 사용 시
```dockerfile
# Dockerfile에서
ENV OPENAI_API_KEY=sk-your-key

# 또는 docker run 시
docker run -e OPENAI_API_KEY=sk-your-key your-app
```

### 클라우드 서비스
- **AWS**: Systems Manager Parameter Store, Secrets Manager
- **Azure**: Key Vault
- **GCP**: Secret Manager
- **Heroku**: Config Vars

## 보안 체크리스트

- [ ] `.gitignore`에 설정 파일들이 포함되어 있는가?
- [ ] 실제 API 키가 Git 히스토리에 없는가?
- [ ] 환경 변수가 올바르게 설정되었는가?
- [ ] 운영 환경에서 안전한 방법으로 API 키를 관리하고 있는가?

## OpenAI API 키 발급

1. [OpenAI Platform](https://platform.openai.com/) 접속
2. 로그인 후 API Keys 메뉴 이동
3. "Create new secret key" 클릭
4. 키 이름 입력 후 생성
5. 생성된 키를 안전한 곳에 저장 (다시 볼 수 없음)

## 문제해결

### 401 Unauthorized 오류 해결

#### 1. API 키 형식 확인
OpenAI API 키는 반드시 `sk-`로 시작해야 합니다.
```
올바른 형식: sk-proj-abc123...
잘못된 형식: proj-abc123... (sk- 접두사 없음)
```

#### 2. API 키 유효성 확인
- OpenAI 플랫폼에서 API 키가 활성화되어 있는지 확인
- 계정에 충분한 크레딧이 있는지 확인
- API 키가 만료되지 않았는지 확인

#### 3. 환경 변수 설정 확인
```bash
# Windows PowerShell에서 확인
echo $env:OPENAI_API_KEY

# macOS/Linux에서 확인
echo $OPENAI_API_KEY
```

#### 4. 프로파일 설정 확인
로컬 설정 파일을 사용하는 경우:
```bash
# 프로파일 활성화 확인
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### 5. API 연결 테스트
```bash
# API 키 테스트 엔드포인트 호출
curl -X GET http://localhost:9000/api/resume/test-api
```

### API 키가 인식되지 않는 경우
1. 환경 변수 설정 확인: `echo $OPENAI_API_KEY` (Linux/Mac) 또는 `echo %OPENAI_API_KEY%` (Windows)
2. IDE 재시작
3. 애플리케이션 재시작
4. 프로파일 설정 확인

### Git에 이미 API 키가 커밋된 경우
```bash
# 히스토리에서 완전 제거 (주의: 협업 시 문제 발생 가능)
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch src/main/resources/application.properties' --prune-empty --tag-name-filter cat -- --all

# 또는 새로운 API 키 발급 후 기존 키 비활성화
``` 