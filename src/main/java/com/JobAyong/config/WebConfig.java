package com.JobAyong.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // 윈도우 전용 경로
            registry.addResourceHandler("/images/**")
                    .addResourceLocations("file:///D:/T2/upload/image/");
        } else {
            // 맥 전용 경로
//            registry.addResourceHandler("/images/**")
//                    .addResourceLocations("file:/Users/sj/Documents/T2/upload/image/");\
        }
    }
}