package com.aws.snapcloud.service;

import java.util.List;

public interface RekognitionService {
    List<String> searchImagesByLabel(String label);
}
