-- 모든 테이블 조회 쿼리

-- user 테이블 조회
SELECT * FROM `user`;

-- company 테이블 조회
SELECT * FROM `company`;

-- target_type 테이블 조회
SELECT * FROM `target_type`;

-- question_list 테이블 조회
SELECT * FROM `question_list`;

-- resume 테이블 조회
SELECT * FROM `resume`;

-- voice 테이블 조회
SELECT * FROM `voice`;

-- interview_archive 테이블 조회
SELECT * FROM `interview_archive`;

-- interview_question 테이블 조회
SELECT * FROM `interview_question`;

-- interview_answer 테이블 조회
SELECT * FROM `interview_answer`;

-- interview_eval 테이블 조회
SELECT * FROM `interview_eval`;

-- voice_eval 테이블 조회
SELECT * FROM `voice_eval`;

-- resume_eval 테이블 조회
SELECT * FROM `resume_eval`;

-- report 테이블 조회
SELECT * FROM `report`;

SELECT * FROM `ticket`;

-- 삭제되지 않은 데이터만 조회하는 쿼리 (deleted_at이 NULL인 데이터)
SELECT * FROM `user` WHERE deleted_at IS NULL;
SELECT * FROM `company` WHERE deleted_at IS NULL;
SELECT * FROM `question_list` WHERE deleted_at IS NULL;
SELECT * FROM `resume` WHERE deleted_at IS NULL;
SELECT * FROM `voice` WHERE deleted_at IS NULL;
SELECT * FROM `interview_archive` WHERE deleted_at IS NULL;
SELECT * FROM `interview_question` WHERE deleted_at IS NULL;
SELECT * FROM `interview_answer` WHERE deleted_at IS NULL;
SELECT * FROM `interview_eval` WHERE deleted_at IS NULL;
SELECT * FROM `voice_eval` WHERE deleted_at IS NULL;
SELECT * FROM `resume_eval` WHERE deleted_at IS NULL;
SELECT * FROM `report` WHERE deleted_at IS NULL; 