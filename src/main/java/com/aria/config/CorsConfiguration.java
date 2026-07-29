package com.aria.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    private final String documentStorageDir;

    public CorsConfiguration(
            @org.springframework.beans.factory.annotation.Value("${earms.document-storage:/tmp/earms-docs}")
            String documentStorageDir) {
        this.documentStorageDir = documentStorageDir;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // no application interceptors (legacy exception-log interceptor removed)
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serves files written by DocumentUploadController / AccountController's
        // avatar upload (both write under earms.document-storage and hand back a
        // "/uploads/..." URL) — without this mapping that URL 404s, there is no
        // controller or handler for it otherwise.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + documentStorageDir + "/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // TODO Auto-generated method stub

    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        // TODO Auto-generated method stub

    }

    @Override
    public Validator getValidator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        // TODO Auto-generated method stub
        return null;
    }
}