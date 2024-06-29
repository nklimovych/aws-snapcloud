package com.aws.snapcloud.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file);

    List<String> listAllFiles();
}
