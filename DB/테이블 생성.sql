drop database JobAyong; 
create database JobAyong;

-- desc interview_question;

-- DB 사용
use JobAyong;
-- select * from users;

-- drop table `interview_answer`;
-- drop table `interview_archive`;
-- drop table `interview_eval`;
-- drop table `interview_question`;

commit;

-- 테이블 생성
-- 테이블 생성 쿼리
-- user 테이블
CREATE TABLE `user` (
    `email` VARCHAR(255) NOT NULL,
    `role` ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    `password` VARCHAR(255) NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `birth` DATE NOT NULL,
    `phone_number` VARCHAR(20) NOT NULL,
    `gender` ENUM('male', 'female') NOT NULL,
    `profile_image` LONGBLOB NULL COMMENT '프로필 사진 바이너리 데이터',
    `original_filename` VARCHAR(255) NULL COMMENT '업로드시 사용자가 올린 파일명',
    `job` VARCHAR(255) NULL,
    `company` VARCHAR(255) NULL,
    `status` ENUM('ACTIVE', 'SUSPENDED', 'DELETE_WAITING') NOT NULL DEFAULT 'ACTIVE' COMMENT '사용자 상태: ACTIVE(활성), SUSPENDED(정지), DELETE_WAITING(탈퇴대기)',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`email`)
);

-- company 테이블
CREATE TABLE `company` (
    `company_id` INT AUTO_INCREMENT NOT NULL COMMENT '회사를 구분하는 고유 식별 번호',
    `name` VARCHAR(255) NOT NULL COMMENT '회사이름, 255길이제한, null불가',
    `size` ENUM('LARGE', 'MEDIUM', 'PUBLIC') NULL COMMENT '회사규모: 대기업, 중소기업(중견), 공기업',
    `industry` VARCHAR(100) NULL COMMENT '회사산업에 대한 설명, null가능, 100길이 제한',
    `employees` INT NULL COMMENT '회사내 사원수, null가능',
    `establishment` DATE NULL COMMENT '회사 설립일, null가능',
    `CEO` VARCHAR(50) NULL COMMENT '회사 대표, null가능, 길이 50 제한',
    `revenue` VARCHAR(50) NULL COMMENT '회사 매출, null가능, 길이 50 제한',
    `address` VARCHAR(255) NULL COMMENT '회사 주소, null가능, 길이 255제한',
    `homepage` VARCHAR(200) NULL COMMENT '회사 공식 홈페이지, null가능, 길이 200제한',
    `history` TEXT NULL COMMENT '회사 연혁, null가능, 길이 255제한',
    `main_business` VARCHAR(255) NULL COMMENT '회사 주요사업, null가능, 길이 255제한',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`company_id`)
);

-- target_type 테이블
CREATE TABLE `target_type` (
    `target_type_id` INT AUTO_INCREMENT NOT NULL,
    `target_type_name` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`target_type_id`)
);

-- question_list 테이블
CREATE TABLE `question_list` (
    `question_id` INT AUTO_INCREMENT NOT NULL,
    `question_type` ENUM('GENERAL', 'PRESSURE', 'PERSONALITY', 'TECHNICAL', 'SITUATIONAL', 'CUSTOM') NOT NULL,
    `question` TEXT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`question_id`)
);

-- resume 테이블
CREATE TABLE `resume` (
    `resume_id` INT AUTO_INCREMENT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `resume_title` VARCHAR(100) NOT NULL DEFAULT '제목 없음',
    `resume_text` TEXT NULL,
    `resume_type` ENUM('text', 'file') NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`resume_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
);

-- voice 테이블
CREATE TABLE `voice` (
    `voice_id` INT AUTO_INCREMENT NOT NULL COMMENT '음성 파일 식별 ID',
    `email` VARCHAR(255) NOT NULL COMMENT '사용자 이메일 (user 테이블 참조)',
    `file_name` VARCHAR(255) NULL COMMENT '업로드된 원본 파일명 (예: uploaded.webm)',
    `file_type` VARCHAR(50) NULL COMMENT '파일 확장자 (webm, mp3 등)',
    `file_size` INT NULL COMMENT '파일 용량 (바이트 단위)',
    `file_path` VARCHAR(500) NULL COMMENT '원본 파일의 저장 경로',
    `converted_file_path` VARCHAR(255) NULL COMMENT 'wav로 변환된 파일 경로',
    `wav_data` LONGBLOB NULL COMMENT 'WAV 파일의 바이너리 데이터',
    `transcript_text` VARCHAR(500) NULL COMMENT 'Whisper로 추출된 음성 텍스트 요약',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업로드 시각',
    `deleted_at` TIMESTAMP NULL COMMENT '삭제 일시 (소프트 삭제용)',

    PRIMARY KEY (`voice_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- interview_archive 테이블
CREATE TABLE `interview_archive` (
    `interview_archive_id` INT AUTO_INCREMENT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `company` VARCHAR(255) NULL,
    `archive_name` VARCHAR(255) NOT NULL,
    `archive_mode` ENUM('GENERAL', 'PRESSURE', 'PERSONALITY', 'TECHNICAL', 'SITUATIONAL', 'CUSTOM') NOT NULL,
    `position` VARCHAR(100) NULL COMMENT '직무/전문분야',
    `status` ENUM('PENDING', 'DONE') NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`interview_archive_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
);

-- interview_question 테이블
CREATE TABLE `interview_question` (
    `interview_question_id` INT AUTO_INCREMENT NOT NULL,
    `interview_archive_id` INT NOT NULL,
    `interview_question_type` ENUM('GENERAL', 'PRESSURE', 'PERSONALITY', 'TECHNICAL', 'SITUATIONAL', 'CUSTOM') NOT NULL,
    `interview_question` TEXT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`interview_question_id`),
    FOREIGN KEY (`interview_archive_id`) REFERENCES `interview_archive` (`interview_archive_id`)
);

-- interview_answer 테이블
CREATE TABLE `interview_answer` (
    `interview_answer_id` INT AUTO_INCREMENT NOT NULL,
    `interview_question_id` INT NOT NULL,
    `interview_archive_id` INT NOT NULL,
    `interview_answer` TEXT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`interview_answer_id`),
    FOREIGN KEY (`interview_question_id`) REFERENCES `interview_question` (`interview_question_id`),
    FOREIGN KEY (`interview_archive_id`) REFERENCES `interview_archive` (`interview_archive_id`)
);

-- interview_eval 테이블
CREATE TABLE `interview_eval` (
    `interview_eval_id` INT AUTO_INCREMENT NOT NULL,
    `interview_archive_id` INT NOT NULL,
    `eval_mode` ENUM('GENERAL', 'PRESSURE', 'PERSONALITY', 'TECHNICAL', 'SITUATIONAL', 'CUSTOM') NOT NULL,
    `eval_score`	INT	NULL,
	`eval_reason`	TEXT	NULL,
	`eval_good_summary`	TEXT	NULL,
	`eval_good_description`	TEXT	NULL,
	`eval_bad_summary`	TEXT	NULL,
	`eval_bad_description`	TEXT	NULL,
	`eval_state`	TEXT	NULL,
	`eval_cause`	TEXT	NULL,
	`eval_solution`	TEXT	NULL,
	`eval_improvment`	TEXT	NULL,
    `prev_summary` TEXT NULL,
    `prev_description` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`interview_eval_id`),
    FOREIGN KEY (`interview_archive_id`) REFERENCES `interview_archive` (`interview_archive_id`)
);

-- voice_eval 테이블
CREATE TABLE `voice_eval` (
  `eval_id`              INT AUTO_INCREMENT COMMENT '음성 평가 결과 ID',
  `voice_id`             INT NOT NULL COMMENT 'voice 테이블의 외래키',

  `transcript`           LONGTEXT COMMENT 'Whisper로 추출한 텍스트',
  `overall_score`        INT COMMENT '전체 종합 점수 (10~100)',
  `clarity_score`        INT COMMENT '명료도 점수',
  `speed_score`          INT COMMENT '속도 점수',
  `volume_score`         INT COMMENT '볼륨 점수',
  `confidence_score`     INT COMMENT '자신감 점수 (overall_score와 동일)',

  `words_per_minute`     INT COMMENT '분당 단어 수',
  `pause_duration`       FLOAT COMMENT '평균 멈춤 시간 (초)',
  `intonation`           INT COMMENT '억양 변화 점수',
  `pronunciation`        INT COMMENT '발음 정확도 점수',
  `fillers_count`        INT COMMENT '간투사(음, 어 등) 사용 횟수',

  `metric_grades_json`   JSON COMMENT '각 지표별 등급 및 짧은 코멘트 (clarity, speed 등)',
  `voice_patterns_json`  JSON COMMENT '음성 패턴 분석 결과 (볼륨, 속도, 톤)',
  `strengths_json`       JSON COMMENT '강점 리스트 [{title, description}]',
  `improvements_json`    JSON COMMENT '개선점 리스트 [{title, description}]',
  `strategies_json`      JSON COMMENT '개선 전략 리스트 [{title, description}]',

  `interviewer_comment`  TEXT COMMENT '면접관 관점 종합 코멘트',
  `created_at`           DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '분석 저장 시각',
  `deleted_at`           DATETIME DEFAULT NULL COMMENT '소프트 삭제 일시',

  PRIMARY KEY (`eval_id`),
  FOREIGN KEY (`voice_id`) REFERENCES `voice`(`voice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- resume_eval 테이블
CREATE TABLE `resume_eval` (
    `resume_eval_id` INT AUTO_INCREMENT NOT NULL,
    `resume_id` INT NULL COMMENT '자기소개서 삭제시에도 평가 데이터 유지를 위해 NULL 허용',
    `resume_eval_title` VARCHAR(255) NULL,
    `email` VARCHAR(255) NOT NULL,
    `resume_org` TEXT NOT NULL COMMENT '평가 받는 텍스트',
    `resume_imp` TEXT NOT NULL COMMENT 'ai가 개선해준 자소서',
    `reason` TEXT NULL COMMENT '개선 이유',
    `missing_areas` TEXT NULL,
    `resume_fin` TEXT NOT NULL COMMENT '수정 저장할 최종 자소서',
    `resume_eval_version` INT NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`resume_eval_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`),
    CONSTRAINT `fk_resume` FOREIGN KEY (`resume_id`) 
    REFERENCES `resume` (`resume_id`) 
    ON DELETE SET NULL 
    ON UPDATE SET NULL
);

-- report 테이블
CREATE TABLE `report` (
    `report_id` INT AUTO_INCREMENT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `target_type_id` INT NOT NULL COMMENT '면접 질문, 면접 평가, 음성 평가 등',
    `target_id` INT NOT NULL COMMENT '신고 대상 테이블의 PK',
    `report_reason` TEXT NULL,
    `status` ENUM('pending', 'done') NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`report_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`),
    FOREIGN KEY (`target_type_id`) REFERENCES `target_type` (`target_type_id`)
); 

CREATE TABLE `ticket` (
	`ticket_id`	INT AUTO_INCREMENT	NOT NULL,
	`email`	VARCHAR(255)	NOT NULL,
	`ticket_comment`	TEXT	NOT NULL,
	`status`	ENUM('pending', 'done')	NULL,
	`created_at`	TIMESTAMP	NULL	DEFAULT CURRENT_TIMESTAMP,
	`deleted_at`	TIMESTAMP	NULL,
    PRIMARY KEY (`ticket_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
);