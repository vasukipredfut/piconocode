package com.aria;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AriaApplication.class, args);
	}

	// Multipart upload now handled by Spring 6's StandardServletMultipartResolver
	// (auto-configured by spring-boot-starter-web). Limits configured via
	// `spring.servlet.multipart.max-file-size` / `max-request-size` in
	// application.properties.

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
}
