package com.aria;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AriaApplication {

	public static void main(String[] args) {
		// Pin the JVM's own default timezone to IST before anything else runs.
		// java.sql.Timestamp has no timezone of its own -- JDBC drivers build
		// it by interpreting a TIMESTAMP column's bare wall-clock digits using
		// whatever the JVM's default zone happens to be at read time, then
		// Timestamp#toString() converts back through that SAME default zone,
		// so it always echoes the original digits regardless of what that
		// default zone is -- masking the bug at every println/toString site.
		// Only the underlying instant (what actually gets serialised to JSON)
		// is wrong. Confirmed live across two deployments that disagreed with
		// EACH OTHER: a host whose JVM default was already Asia/Kolkata (this
		// dev machine, and one deployment) read a stored "11:35:39" IST
		// correctly; a host whose JVM default was UTC read the exact same
		// digits as 11:35:39 UTC -- a genuinely different, 5:30h-off instant,
		// not just a mislabelled one. A `connectionTimeZone` JDBC URL
		// parameter does NOT fix this (verified: it has no effect on the
		// legacy `ResultSet#getTimestamp()` path Spring's JdbcTemplate uses);
		// only pinning the JVM's own default zone does. Paired with
		// spring.jackson.time-zone=Asia/Kolkata (application.properties) on
		// the write/serialisation side, every timestamp now round-trips
		// consistently regardless of a given host's own OS/system timezone.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
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
