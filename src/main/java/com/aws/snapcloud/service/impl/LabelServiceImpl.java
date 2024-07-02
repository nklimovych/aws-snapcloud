package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.entity.Label;
import com.aws.snapcloud.repository.LabelRepository;
import com.aws.snapcloud.service.LabelService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;

    @Override
    public List<String> findTopLabels(int limit) {
        return labelRepository.findTopLabels(limit);
    }

    @Override
    public List<Label> findAll() {
        return labelRepository.findAll();
    }

    @Override
    public List<Label> saveAll(List<Label> labels) {
        return labelRepository.saveAll(labels);
    }
}
