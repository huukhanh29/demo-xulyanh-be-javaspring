package com.example.xulyanh.controller;


import com.example.xulyanh.entity.LuuAnh;
import com.example.xulyanh.repository.LuuAnhRepository;
import com.example.xulyanh.response.MessageResponse;
import com.example.xulyanh.service.FilesStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class FilesController {

    @Autowired
    FilesStorageService storageService;
    @Autowired
    private LuuAnhRepository luuAnhRepository;
    //lấy danh sách đối tượng
    @GetMapping("/list")
    public ResponseEntity<?> getLuuAnhList() {
        List<LuuAnh> luuAnhList = luuAnhRepository.findAll();
        return ResponseEntity.ok(luuAnhList);
    }
    //tải ảnh lên server
    @PostMapping
    public ResponseEntity<?> createTaiLieu(@RequestParam("file") MultipartFile file, @RequestParam("name") String name) {
        try {
            String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));

            // chuyển ten file vê ko dau
            baseName = createSlug(baseName);

            // Tạo thời gian kiểu số
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            //dat ten file theo biến thời gian -> tránh trùng tên file
            String newFileName = timestamp + "_" + baseName + fileExtension;

            storageService.saveRandom(file, newFileName);
            LuuAnh luuAnh = new LuuAnh();
            luuAnh.setName(name);
            luuAnh.setUrl(newFileName);
            return ResponseEntity.ok(luuAnhRepository.save(luuAnh));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new MessageResponse("Could not create TaiLieu. Error: " + e.getMessage()));
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return "";
    }


    private String extractOriginalFileName(String currentFileName) {
        int underscoreIndex = currentFileName.indexOf("_");
        if (underscoreIndex > 0 && underscoreIndex < currentFileName.length() - 1) {
            return currentFileName.substring(underscoreIndex + 1);
        }
        return currentFileName;
    }
    //tải anh theo mã
    @GetMapping("/{maLuuAnh}/download")
    public ResponseEntity<?> downloadFile(@PathVariable Long maLuuAnh) {
        Optional<LuuAnh> taiLieu = luuAnhRepository.findById(maLuuAnh);
        if (taiLieu.isPresent()) {
            try {
                String fileName = taiLieu.get().getUrl();
                Resource file = storageService.load(fileName); // Giả định storageService có một hàm load() để lấy file dựa trên tên

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
            }
        } else {
            return new ResponseEntity<>(new MessageResponse("Tài liệu không tồn tại"), HttpStatus.NOT_FOUND);
        }
    }
    //xem file theo ten
    @GetMapping("/anh/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = storageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    //hàm chuyển về ko dấu
    public static String createSlug(String string) {
        String[] search = {
                "(à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ)",
                "(è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ)",
                "(ì|í|ị|ỉ|ĩ)",
                "(ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ)",
                "(ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ)",
                "(ỳ|ý|ỵ|ỷ|ỹ)",
                "(đ)",
                "(À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ)",
                "(È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ)",
                "(Ì|Í|Ị|Ỉ|Ĩ)",
                "(Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ)",
                "(Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ)",
                "(Ỳ|Ý|Ỵ|Ỷ|Ỹ)",
                "(Đ)",
                "[^a-zA-Z0-9\\-_]"
        };

        String[] replace = {
                "a",
                "e",
                "i",
                "o",
                "u",
                "y",
                "d",
                "A",
                "E",
                "I",
                "O",
                "U",
                "Y",
                "D",
                "-"
        };

        String temp = string;

        for (int i = 0; i < search.length; i++) {
            temp = temp.replaceAll(search[i], replace[i]);
        }

        temp = temp.replaceAll("(-)+", " ");
        temp = temp.toLowerCase();

        // Remove diacritics using Normalizer
        temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
        temp = temp.replaceAll("[^\\p{ASCII}]", "");

        return temp;
    }
}
