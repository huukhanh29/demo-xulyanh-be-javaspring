package com.example.xulyanh;

import com.example.xulyanh.service.FilesStorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//implement CommandLine
@SpringBootApplication
public class XulyanhApplication implements CommandLineRunner {
    @Resource
    FilesStorageService storageService;
    public static void main(String[] args) {
        SpringApplication.run(XulyanhApplication.class, args);
    }
   //Overide thêm storage
    @Override
    public void run(String... arg) throws Exception {
        storageService.init();
    }
}
