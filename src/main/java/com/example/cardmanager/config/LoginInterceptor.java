package com.example.cardmanager.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        String path = request.getRequestURI();

       
        if (path.startsWith("/login") ||
            path.startsWith("/signup") ||      
            path.startsWith("/css") ||
            path.startsWith("/js") ||
            path.startsWith("/images") ||
            path.startsWith("/uploaded-images")) { 
            return true;
        }

   
        if (session == null || session.getAttribute("loginUser") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
