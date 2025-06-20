package com.JobAyong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class JobAyongApplication {

	public static void main(String[] args) {
		// JVM의 기본 시간대를 한국 시간으로 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(JobAyongApplication.class, args);
	}

}
