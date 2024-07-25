package com.aws.snapcloud.service;

import java.util.List;

public interface RekognitionService {
    List<String> detectLabels(String key);
}
