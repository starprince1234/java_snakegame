package com.example.snake.filter;



import jakarta.servlet.*;

import jakarta.servlet.annotation.WebFilter;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;



// NoCacheFilter.java

public class NoCacheFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)

            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        response.setHeader("Pragma", "no-cache");

        response.setDateHeader("Expires", 0);

        chain.doFilter(req, res); // 必须调用此方法放行请求

    }

}