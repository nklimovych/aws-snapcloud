package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.dto.ImageResponseDto;
import com.aws.snapcloud.entity.Image;
import com.aws.snapcloud.exception.DuplicateEntityException;
import com.aws.snapcloud.mapper.ImageMapper;
import com.aws.snapcloud.repository.ImageRepository;
import com.aws.snapcloud.service.ImageService;
import com.aws.snapcloud.service.RekognitionService;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private static final int INITIATE_IMAGE_COUNT = 38;

    private final RekognitionService rekognitionService;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public ImageResponseDto upload(MultipartFile file) {
        String name = file.getOriginalFilename();

        if (imageRepository.existsByName(name)) {
            throw new DuplicateEntityException("Image with the same name already exists: " + name);
        }

        try {
            RequestBody body = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                                                             .bucket(bucketName)
                                                             .key(name)
                                                             .build();
            s3Client.putObject(objectRequest, body);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + name, e);
        }
        return imageMapper.toResponseDto(save(name));
    }

    @Override
    public List<String> searchByLabel(String label) {
        return imageRepository.findByLabelName(label).stream()
                              .map(Image::getUrl)
                              .toList();
    }

    @Override
    public List<String> getRandomImageUrls() {
        List<String> randomUrls = new ArrayList<>();
        List<Image> images = imageRepository.findAll();
        int count = INITIATE_IMAGE_COUNT;

        if (images.size() <= count) {
            return images.stream()
                         .map(Image::getUrl)
                         .toList();
        }

        Random random = new Random();
        Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < count) {
            indexes.add(random.nextInt(images.size()));
        }

        for (Integer index : indexes) {
            randomUrls.add(images.get(index).getUrl());
        }

        return randomUrls;
    }

    private Image save(String fileName) {
        Image image = new Image();
        image.setName(fileName);
        image.setUrl(getUrl(fileName));
        image.setLabels(rekognitionService.detectLabels(fileName));
        return imageRepository.save(image);
    }

    private String getUrl(String key) {
        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
                                                           .bucket(bucketName)
                                                           .key(key)
                                                           .build());
        return url.toString();
    }
}
