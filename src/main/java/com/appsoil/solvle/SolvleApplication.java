package com.appsoil.solvle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SolvleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolvleApplication.class, args);
	}

}
