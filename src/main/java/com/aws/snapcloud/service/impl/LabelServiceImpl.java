package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.entity.Label;
import com.aws.snapcloud.repository.LabelRepository;
import com.aws.snapcloud.service.LabelService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;

    @Override
    public Optional<Label> findByName(String name) {
        return labelRepository.findByName(name);
    }

    @Override
    public Label save(Label label) {
        return labelRepository.save(label);
    }

    @Override
    public List<String> findTopLabels(int limit) {
        return labelRepository.findTopLabels(limit);
    }
}
