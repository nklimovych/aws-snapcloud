package com.aws.snapcloud.service;

import java.util.Set;

public interface RekognitionService {
    Set<String> detectLabels(String key);
}
