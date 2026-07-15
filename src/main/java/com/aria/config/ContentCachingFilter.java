package com.aria.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.util.ContentCachingResponseWrapper;

public class ContentCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Wrap the response
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        try {
            // Proceed with the filter chain
            chain.doFilter(request, responseWrapper);
        } finally {
            // Copy the cached body back to the original response to ensure the client gets the response
            responseWrapper.copyBodyToResponse();
        }
    }
}
