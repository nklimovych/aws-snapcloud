package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.service.LabelService;
import com.aws.snapcloud.service.RekognitionService;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.S3Object;

@Service
@RequiredArgsConstructor
public class RekognitionServiceImpl implements RekognitionService {
    private final RekognitionClient rekognitionClient;
    private final LabelService labelService;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public List<String> detectLabels(String key) {
        DetectLabelsRequest request = DetectLabelsRequest.builder()
                                                         .image(getImage(key))
                                                         .minConfidence(70F)
                                                         .maxLabels(10)
                                                         .build();
        DetectLabelsResponse detectedLabels = rekognitionClient.detectLabels(request);

        HashSet<Label> labels = new HashSet<>(detectedLabels.labels());
        return labelService.saveLabels(labels, key);
    }

    private Image getImage(String key) {
        return Image.builder()
                    .s3Object(
                            S3Object.builder()
                                    .bucket(bucketName)
                                    .name(key)
                                    .build())
                    .build();
    }
}
