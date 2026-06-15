package com.example.cardmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

        @Autowired
    private LocaleChangeInterceptor localeChangeInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploaded-images/**")
                .addResourceLocations("file:./uploads/");
    }

        @Override
    public void addInterceptors(InterceptorRegistry registry) {

                registry.addInterceptor(localeChangeInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/cards/**")
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/signup",
                        "/logout",
                        "/error",
                        "/h2-console/**",
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "/uploaded-images/**",
                        "/uploads/**",
                        "/favicon.ico"
                );
    }
}
