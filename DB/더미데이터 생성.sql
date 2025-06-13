-- 더미데이터 삽입 쿼리

-- user 테이블 더미데이터
INSERT INTO `user` (`email`, `role`, `password`, `name`, `birth`, `phone_number`, `gender`) VALUES
('user1@example.com', 'user', 'password123', '홍길동', '1990-01-01', '010-1234-5678', 'male'),
('user2@example.com', 'user', 'password123', '김철수', '1992-05-15', '010-2345-6789', 'male'),
('admin@example.com', 'admin', '$2a$10$EM5HoNM6z3k.xKZICyfM3.uqIh792ZmV6/QlpnyNl1qZVBMWBZRJC', '관리자', '1985-12-31', '010-3456-7890', 'female');

-- company 테이블 더미데이터
INSERT INTO `company` (`name`, `size`, `industry`, `employees`, `establishment`, `CEO`, `revenue`, `address`, `homepage`) VALUES
('삼성전자', 'large', '전자제품', 100000, '1969-01-13', '김기남', '100조원', '서울시 서초구', 'www.samsung.com'),
('네이버', 'large', 'IT서비스', 5000, '1999-06-02', '한성숙', '5조원', '경기도 성남시', 'www.naver.com'),
('스타트업A', 'medium', '소프트웨어', 100, '2020-01-01', '이영희', '10억원', '서울시 강남구', 'www.startupA.com');

-- target_type 테이블 더미데이터
INSERT INTO `target_type` (`target_type_name`) VALUES
('면접 질문'),
('면접 평가'),
('음성 평가');

-- question_list 테이블 더미데이터
INSERT INTO `question_list` (`question_type`, `question`) VALUES
('gen', '자기소개를 해주세요.'),
('tech', '프로젝트에서 가장 어려웠던 점은 무엇인가요?'),
('sit', '팀원과 갈등이 생겼을 때 어떻게 해결하시나요?');

-- resume 테이블 더미데이터
INSERT INTO `resume` (`email`, `resume_title`, `resume_text`, `resume_type`) VALUES
('user1@example.com', '신입 개발자 이력서', '안녕하세요. 신입 개발자 홍길동입니다...', 'text'),
('user2@example.com', '경력 개발자 이력서', '안녕하세요. 3년차 개발자 김철수입니다...', 'text'),
('user1@example.com', '포트폴리오', '프로젝트 경험: 1. 웹사이트 개발...', 'text');

-- voice 테이블 더미데이터
INSERT INTO `voice` (`email`, `file_name`, `file_type`, `file_size`, `file_path`) VALUES
('user1@example.com', 'interview1.mp3', 'audio/mp3', 1024, '/uploads/voice/interview1.mp3'),
('user2@example.com', 'interview2.mp3', 'audio/mp3', 2048, '/uploads/voice/interview2.mp3'),
('user1@example.com', 'interview3.mp3', 'audio/mp3', 3072, '/uploads/voice/interview3.mp3');

-- interview_archive 테이블 더미데이터
INSERT INTO `interview_archive` (`email`, `company_id`, `position`, `status`) VALUES
('user1@example.com', 1, '마케팅·광고·MD > 퍼포먼스마케터 > 카페·블로그관리', 'done'),
('user2@example.com', 2, '금융·보험 > 보험설계사 > 보험사고', 'pending'),
('user1@example.com', 3, '제조·생산 > 생산직종사자 > 조립', 'done');

-- interview_question 테이블 더미데이터
INSERT INTO `interview_question` (`interview_archive_id`, `interview_question_type`, `interview_question`) VALUES
(1, 'gen', '자기소개를 해주세요.'),
(1, 'tech', '프로젝트 경험에 대해 말씀해주세요.'),
(2, 'sit', '팀 프로젝트에서의 역할은 무엇이었나요?');

-- interview_answer 테이블 더미데이터
INSERT INTO `interview_answer` (`interview_question_id`, `interview_archive_id`, `interview_answer`) VALUES
(1, 1, '안녕하세요. 홍길동입니다. 컴퓨터공학을 전공했습니다.'),
(2, 1, '웹 프로젝트를 진행했고, 프론트엔드 개발을 담당했습니다.'),
(3, 2, '팀장으로서 프로젝트 일정 관리와 팀원 조율을 담당했습니다.');

-- interview_eval 테이블 더미데이터
INSERT INTO `interview_eval` (`interview_answer_id`, `interview_archive_id`, `eval_comment`, `eval_score`) VALUES
(1, 1, '자기소개가 명확하고 간결합니다.', 8),
(2, 1, '프로젝트 경험을 잘 설명했습니다.', 9),
(3, 2, '팀장 경험이 인상적입니다.', 7);

-- voice_eval 테이블 더미데이터
INSERT INTO `voice_eval` (`voice_id`, `email`, `voice_eval_comment`, `voice_eval_score`) VALUES
(1, 'user1@example.com', '발음이 매우 명확합니다.', 9),
(2, 'user2@example.com', '목소리가 조금 작습니다.', 7),
(3, 'user1@example.com', '적절한 톤과 속도로 말했습니다.', 8);

-- resume_eval 테이블 더미데이터
INSERT INTO `resume_eval` (`resume_id`, `email`, `resume_eval_comment`, `resume_eval_score`, `resume_org`, `resume_log`, `position`) VALUES
(1, 'user1@example.com', '이력서가 잘 구성되어 있습니다.', 8, '원본 이력서 내용', '수정된 이력서 내용', '신입 개발자'),
(2, 'user2@example.com', '경력 사항이 상세합니다.', 9, '원본 이력서 내용', '수정된 이력서 내용', '시니어 개발자'),
(3, 'user1@example.com', '포트폴리오가 인상적입니다.', 8, '원본 이력서 내용', '수정된 이력서 내용', '주니어 개발자');

-- report 테이블 더미데이터
INSERT INTO `report` (`email`, `target_type_id`, `target_id`, `report_reason`, `status`) VALUES
('user1@example.com', 1, 1, '부적절한 질문입니다.', 'pending'),
('user2@example.com', 2, 1, '부정확한 평가입니다.', 'done'),
('user1@example.com', 3, 1, '음성 품질이 좋지 않습니다.', 'pending'); 