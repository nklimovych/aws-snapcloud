package com.aws.snapcloud.service;

import com.aws.snapcloud.entity.Label;
import java.util.Set;

public interface RekognitionService {
    Set<Label> detectLabels(String key);
}
