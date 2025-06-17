drop database JobAyong; 
create database JobAyong;

desc interview_question;


-- DB 사용
use JobAyong;
-- select * from users;

drop table `interview_answer`;
drop table `interview_archive`;
drop table `interview_eval`;
drop table `interview_question`;

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
    `profile_image` VARCHAR(255) NULL COMMENT '프로필 사진 파일 url',
    `original_filename` VARCHAR(255) NULL COMMENT '업로드시 사용자가 올린 파일명',
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
    `question_type` ENUM('gen', 'str', 'per', 'tech', 'sit', 'cus') NOT NULL,
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
    `resume_text` TEXT NOT NULL,
    `resume_file` VARCHAR(255) NULL,
    `resume_type` ENUM('text', 'file') NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`resume_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
);

-- voice 테이블
CREATE TABLE `voice` (
    `voice_id` INT AUTO_INCREMENT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `file_name` VARCHAR(255) NULL,
    `file_type` VARCHAR(50) NULL,
    `file_size` INT NULL,
    `file_path` VARCHAR(500) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`voice_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
);

-- interview_archive 테이블
CREATE TABLE `interview_archive` (
    `interview_archive_id` INT AUTO_INCREMENT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `company_id` INT NULL COMMENT '회사를 구분하는 고유 식별 번호',
    `position` VARCHAR(100) NULL COMMENT '직무/전문분야',
    `status` ENUM('PENDING', 'DONE') NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`interview_archive_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`),
    FOREIGN KEY (`company_id`) REFERENCES `company` (`company_id`)
);

-- interview_question 테이블
CREATE TABLE `interview_question` (
    `interview_question_id` INT AUTO_INCREMENT NOT NULL,
    `interview_archive_id` INT NOT NULL,
    `interview_question_type` ENUM('GENERAL', 'PRESSURE', 'PERSONALITY', 'TECHNICAL', 'SITUATIONAL') NOT NULL,
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
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`interview_eval_id`),
    FOREIGN KEY (`interview_archive_id`) REFERENCES `interview_archive` (`interview_archive_id`)
);

-- voice_eval 테이블
CREATE TABLE `voice_eval` (
    `voice_eval_id` INT AUTO_INCREMENT NOT NULL,
    `voice_id` INT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `voice_eval_comment` TEXT NULL,
    `voice_eval_score` INT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`voice_eval_id`),
    FOREIGN KEY (`voice_id`) REFERENCES `voice` (`voice_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
);

-- resume_eval 테이블
CREATE TABLE `resume_eval` (
    `resume_eval_id` INT AUTO_INCREMENT NOT NULL,
    `resume_id` INT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `resume_eval_comment` TEXT NULL,
    `resume_eval_score` INT NULL,
    `resume_org` TEXT NOT NULL COMMENT '평가 받는 텍스트',
    `resume_log` TEXT NOT NULL COMMENT 'ai가 수정해준 부분에서 사용자가 원하는 부분만 커밋한 텍스트 전체',
    `resume_eval_version` INT NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL,
    `position` VARCHAR(100) NULL,
    PRIMARY KEY (`resume_eval_id`),
    FOREIGN KEY (`resume_id`) REFERENCES `resume` (`resume_id`),
    FOREIGN KEY (`email`) REFERENCES `user` (`email`)
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