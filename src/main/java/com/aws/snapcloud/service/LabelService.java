package com.aws.snapcloud.service;

import com.aws.snapcloud.entity.Label;
import java.util.List;

public interface LabelService {
    List<String> findTopLabels(int limit);

    List<Label> saveAll(List<Label> labels);

    List<Label> findAll();
}
