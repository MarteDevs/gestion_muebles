package com.muebleria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/img/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("classpath:/static/imagenes/")
                .setCachePeriod(3600);
    }
}
