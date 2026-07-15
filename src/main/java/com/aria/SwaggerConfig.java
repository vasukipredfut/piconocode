package com.aria;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aria.config.ContentCachingFilter;

/**
 * Legacy class name kept for git history. Springfox/Docket setup removed;
 * OpenAPI is now served by springdoc-openapi (paths configured in
 * application.properties: springdoc.api-docs.path / springdoc.swagger-ui.path).
 *
 * Only the response-body capturing filter registration remains here, which
 * is needed by ResponseLoggingInterceptor.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public FilterRegistrationBean<ContentCachingFilter> loggingFilter() {
        FilterRegistrationBean<ContentCachingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ContentCachingFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
