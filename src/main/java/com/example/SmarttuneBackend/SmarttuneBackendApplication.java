package com.example.SmarttuneBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmarttuneBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmarttuneBackendApplication.class, args);
	}

}

