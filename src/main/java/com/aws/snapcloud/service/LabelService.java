package com.aws.snapcloud.service;

import java.util.List;
import java.util.Set;
import software.amazon.awssdk.services.rekognition.model.Label;

public interface LabelService {
    List<String> findTopLabels(int limit);

    List<String> saveLabels(Set<Label> labelNames, String key);
}
