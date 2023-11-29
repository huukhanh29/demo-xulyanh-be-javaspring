package com.example.xulyanh.sercurity;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {
    //tạo thư mục lưu trữ file
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**").addResourceLocations("file:images/");
        //registry.addResourceHandler("/Image/**").addResourceLocations("file:///" + System.getProperty("user.dir") + "/src/main/upload/");
    }
}
