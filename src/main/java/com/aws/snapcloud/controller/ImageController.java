package com.aws.snapcloud.controller;

import com.aws.snapcloud.dto.ImageResponseDto;
import com.aws.snapcloud.service.ImageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload")
    public ImageResponseDto uploadImage(@RequestParam("file") MultipartFile file) {
        return imageService.upload(file);
    }

    @GetMapping("/search")
    public List<String> searchImages(@RequestParam("label") String label) {
        return imageService.searchByLabel(label);
    }
}
