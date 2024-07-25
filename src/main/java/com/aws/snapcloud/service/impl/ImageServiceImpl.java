package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.dto.ImageResponseDto;
import com.aws.snapcloud.exception.DuplicateEntityException;
import com.aws.snapcloud.service.ImageService;
import com.aws.snapcloud.service.RekognitionService;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private static final int INITIATE_IMAGE_COUNT = 38;

    private final RekognitionService rekognitionService;
    private final S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public ImageResponseDto upload(MultipartFile file) {
        String name = file.getOriginalFilename();

        if (doesObjectExist(name)) {
            throw new DuplicateEntityException("Image with the same name already exists: " + name);
        }

        try {
            RequestBody body = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                                                             .bucket(bucketName)
                                                             .key(name)
                                                             .build();
            s3Client.putObject(objectRequest, body);
            rekognitionService.detectLabels(name);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + name, e);
        }

        return ImageResponseDto.builder()
                               .name(name)
                               .url(getUrl(name))
                               .build();
    }

    @Override
    public List<String> searchByLabel(String label) {
        List<String> matchingUrls = new ArrayList<>();
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                                                                      .bucket(bucketName)
                                                                      .build();
        ListObjectsV2Response listObjectsResponse;
        do {
            listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

            List<S3Object> s3Objects = listObjectsResponse.contents();
            for (S3Object s3Object : s3Objects) {
                String key = s3Object.key();
                try {
                    GetObjectTaggingResponse taggingResponse = getObjectTags(key);
                    List<Tag> tags = taggingResponse.tagSet();
                    boolean hasLabel = tags.stream()
                                           .anyMatch(tag -> tag.key().startsWith("label-") && label.equals(tag.value()));
                    if (hasLabel) {
                        matchingUrls.add(getUrl(key));
                    }
                } catch (Exception e) {
                    // Handle or log exception if needed
                    System.err.println("Failed to get tags for key: " + key + " due to: " + e.getMessage());
                }
            }

            listObjectsRequest = listObjectsRequest.toBuilder()
                                                   .continuationToken(listObjectsResponse.nextContinuationToken())
                                                   .build();
        } while (listObjectsResponse.isTruncated());

        return matchingUrls;
    }


    @Override
    public List<String> getRandomImageUrls() {
        List<String> randomUrls = new ArrayList<>();
        List<String> imageUrls = getAllImageUrls();

        if (imageUrls.size() <= INITIATE_IMAGE_COUNT) {
            return imageUrls;
        }

        Random random = new Random();
        Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < INITIATE_IMAGE_COUNT) {
            indexes.add(random.nextInt(imageUrls.size()));
        }

        for (Integer index : indexes) {
            randomUrls.add(imageUrls.get(index));
        }

        return randomUrls;
    }

    private boolean doesObjectExist(String key) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> getAllImageUrls() {
        List<String> urls = new ArrayList<>();
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                                                                      .bucket(bucketName)
                                                                      .build();
        ListObjectsV2Response listObjectsResponse;
        do {
            listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

            List<S3Object> s3Objects = listObjectsResponse.contents();
            for (S3Object s3Object : s3Objects) {
                urls.add(getUrl(s3Object.key()));
            }

            listObjectsRequest = listObjectsRequest.toBuilder()
                                                   .continuationToken(
                                                       listObjectsResponse.nextContinuationToken())
                                                   .build();
        } while (listObjectsResponse.isTruncated());

        return urls;
    }

    private String getUrl(String key) {
        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
                                                           .bucket(bucketName)
                                                           .key(key)
                                                           .build());
        return url.toString();
    }

    private GetObjectTaggingResponse getObjectTags(String key) {
        return s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                                                                .bucket(bucketName)
                                                                .key(key)
                                                                .build());
    }
}
