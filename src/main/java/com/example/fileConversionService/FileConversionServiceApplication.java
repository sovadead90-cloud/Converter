package com.example.fileConversionService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileConversionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileConversionServiceApplication.class, args);
	}

}
