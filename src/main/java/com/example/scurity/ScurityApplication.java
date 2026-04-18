package com.example.scurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ScurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScurityApplication.class, args);
	}

}
