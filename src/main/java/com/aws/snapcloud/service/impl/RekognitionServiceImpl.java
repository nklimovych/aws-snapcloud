package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.entity.Label;
import com.aws.snapcloud.service.LabelService;
import com.aws.snapcloud.service.RekognitionService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

@Service
@RequiredArgsConstructor
public class RekognitionServiceImpl implements RekognitionService {
    private final RekognitionClient rekognitionClient;
    private final LabelService labelService;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public Set<Label> detectLabels(String key) {
        DetectLabelsRequest request = DetectLabelsRequest.builder()
                                                         .image(getImage(key))
                                                         .minConfidence(95F)
                                                         .build();

        DetectLabelsResponse response = rekognitionClient.detectLabels(request);

        Set<Label> labels = new HashSet<>();
        for (software.amazon.awssdk.services.rekognition.model.Label rekLabel : response.labels()) {
            String labelName = rekLabel.name();
            Label label = labelService.findByName(labelName).orElseGet(() -> saveLabel(labelName));
            labels.add(label);
        }
        return labels;
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

    private Label saveLabel(String labelName) {
        Label label = new Label();
        label.setName(labelName);
        return labelService.save(label);
    }
}
