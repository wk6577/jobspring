INSERT INTO company (name, size, industry, employees, establishment, CEO, revenue, address, homepage, history)
VALUES
('삼성전자', '대기업', '전자제품', 100000, '1969-01-13', '이재용', '300조', '서울특별시 서초구', 'http://www.samsung.com', '반도체, 스마트폰 사업 등 주요 사업 영역.'),
('카카오', '중소기업', 'IT 및 모바일 서비스', 10000, '2010-03-18', '홍은택', '4조', '제주특별자치도 제주시', 'https://www.kakao.com', '카카오톡 기반 다양한 서비스 제공.'),
('한국전력공사', '공기업', '에너지', 23000, '1961-07-01', '정승일', '60조', '전라남도 나주시', 'https://home.kepco.co.kr', '국내 최대 전력 공급 기업.');


INSERT INTO users (email, role, password, name, birth, phone_number, gender, profile_image, original_filename)
VALUES
('user1@example.com', 'user', 'password1', '홍길동', '1990-01-01', '010-1234-5678', 'male', NULL, NULL),
('admin@example.com', 'admin', 'adminpass', '김영희', '1985-05-05', '010-5555-5555', 'female', NULL, NULL),
('user2@example.com', 'user', 'password2', '박철수', '1995-12-12', '010-9999-8888', 'male', NULL, NULL);


INSERT INTO interview_archive (email, status)
VALUES
('user1@example.com', 'pending'),
('admin@example.com', 'done'),
('user2@example.com', 'done');


INSERT INTO interview_question (interview_archive_id, company_id, interview_question_type, interview_question)
VALUES
(1, 1, 'gen', '자기소개를 해주세요.'),
(2, 2, 'tech', 'Spring Boot에서 의존성 주입 방식에 대해 설명해주세요.'),
(3, 3, 'per', '가장 힘들었던 경험은 무엇인가요?');


INSERT INTO interview_answer (interview_question_id, interview_archive_id, interview_answer)
VALUES
(1, 1, '안녕하세요. 저는 적극적이고 열정적인 개발자입니다.'),
(2, 2, 'Spring에서는 생성자 주입을 가장 권장합니다. 이유는...'),
(3, 3, '대학 시절 프로젝트에서 팀 갈등을 해결한 경험이 있습니다.');


INSERT INTO interview_eval (interview_answer_id, interview_archive_id, eval_comment, eval_score)
VALUES
(1, 1, '자신감 있게 잘 표현했습니다.', 8),
(2, 2, '기술적인 이해도가 높습니다.', 9),
(3, 3, '조금 더 구체적인 설명이 필요합니다.', 7);

INSERT INTO voice (email, file_name, file_type, file_size, file_path)
VALUES
('user1@example.com', 'intro.mp3', 'audio/mpeg', 123456, '/voices/user1_intro.mp3'),
('admin@example.com', 'answer.mp3', 'audio/mpeg', 654321, '/voices/admin_answer.mp3'),
('user2@example.com', 'response.mp3', 'audio/mpeg', 789012, '/voices/user2_response.mp3');


INSERT INTO voice_eval (voice_id, email, voice_eval_comment, voice_eval_score)
VALUES
(1, 'user1@example.com', '목소리가 또렷하고 전달력이 좋습니다.', 9),
(2, 'admin@example.com', '조금 더 천천히 말하는 것이 좋겠습니다.', 7),
(3, 'user2@example.com', '발음이 불분명한 부분이 있습니다.', 6);


INSERT INTO target_type (target_type_name)
VALUES
('interview_question'),
('interview_eval'),
('voice_eval');

INSERT INTO question_list (question_type, question)
VALUES
('gen', '지원 동기를 말씀해주세요.'),
('tech', 'REST API와 SOAP의 차이는 무엇인가요?'),
('sit', '업무 중 위기 상황에서 어떻게 대처했나요?');


INSERT INTO resume (email, resume_title, resume_text, resume_file, resume_type)
VALUES
('user1@example.com', '백엔드 개발자 이력서', '이력서 본문 내용입니다.', NULL, 'text'),
('admin@example.com', '관리자 지원서', '관리자 포지션 이력서입니다.', NULL, 'text'),
('user2@example.com', '프론트엔드 개발자 이력서', 'React, Vue.js 경험 기술기술기술', NULL, 'text');


INSERT INTO resume_eval (resume_id, email, resume_eval_comment, resume_eval_score, resume_org, resume_log)
VALUES
(1, 'user1@example.com', '전반적으로 잘 작성됨. 기술 스택 명확.', 9, '이력서 본문 내용입니다.', '이력서 본문 내용입니다.'),
(2, 'admin@example.com', '관리 역량 강조 필요.', 7, '관리자 포지션 이력서입니다.', '관리자 포지션 이력서입니다.'),
(3, 'user2@example.com', '기술 스택 다양하나 설명 부족.', 6, 'React, Vue.js 경험 기술기술기술', 'React, Vue.js 경험 기술기술기술');


INSERT INTO report (email, target_type_id, target_id, report_reason, status)
VALUES
('user1@example.com', 1, 1, '질문 내용이 부적절합니다.', 'pending'),
('admin@example.com', 2, 1, '평가가 불공정합니다.', 'done'),
('user2@example.com', 3, 3, '음성 품질이 매우 낮습니다.', 'pending');

