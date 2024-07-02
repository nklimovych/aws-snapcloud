package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.entity.Label;
import com.aws.snapcloud.service.LabelService;
import com.aws.snapcloud.service.RekognitionService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
    private static final float CONFIDENCE = 95F;
    private final RekognitionClient rekognitionClient;
    private final LabelService labelService;
    private final Map<String, Label> labelMap = new ConcurrentHashMap<>();

    @Value("${aws.bucket.name}")
    private String bucketName;

    @PostConstruct
    private void init() {
        labelService.findAll().forEach(label -> labelMap.put(label.getName(), label));
    }

    @Override
    public Set<Label> detectLabels(String key) {
        DetectLabelsResponse response = rekognitionClient.detectLabels(
                DetectLabelsRequest.builder()
                                   .image(getImage(key))
                                   .minConfidence(CONFIDENCE)
                                   .build());

        Set<Label> labels = new HashSet<>();
        List<String> newLabelNames = new ArrayList<>();
        for (software.amazon.awssdk.services.rekognition.model.Label rekLabel : response.labels()) {
            String labelName = rekLabel.name();
            Label label = labelMap.get(labelName);
            if (label == null) {
                newLabelNames.add(labelName);
            } else {
                labels.add(label);
            }
        }

        if (!newLabelNames.isEmpty()) {
            List<Label> newLabels = saveLabels(newLabelNames);
            labels.addAll(newLabels);
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

    private List<Label> saveLabels(List<String> labelNames) {
        List<Label> newLabels = labelNames.stream()
                                          .map(name -> {
                                              Label label = new Label();
                                              label.setName(name);
                                              return label;
                                          })
                                          .collect(Collectors.toList());
        List<Label> savedLabels = labelService.saveAll(newLabels);
        savedLabels.forEach(label -> labelMap.put(label.getName(), label));
        return savedLabels;
    }
}
