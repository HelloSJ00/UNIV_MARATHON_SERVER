package com.runningRank.runningRank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // 캐싱 기능 활성화
public class RunningRankApplication {

	public static void main(String[] args) {
		SpringApplication.run(RunningRankApplication.class, args);
	}

}
