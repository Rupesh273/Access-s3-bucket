package com.S3_Bucket.Access_s3_bucket.Controller;

import com.S3_Bucket.Access_s3_bucket.S3Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class S3Controller {
    @Autowired
    private S3Service s3Service;
    @GetMapping("/s3")
    public String getObject(){
        return "You access the s3 bucket";
    }
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        try {
            return s3Service.uploadImage(file,"adityakpmgdata", originalFilename);
        } catch (Exception e) {
            e.printStackTrace();
            return "Upload failed: " + e.getMessage();
        }
    }
}
