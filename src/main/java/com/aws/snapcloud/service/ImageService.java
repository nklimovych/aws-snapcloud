package com.aws.snapcloud.service;

import com.aws.snapcloud.dto.ImageResponseDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    ImageResponseDto upload(MultipartFile file);

    List<String> searchByLabel(String label);

}
