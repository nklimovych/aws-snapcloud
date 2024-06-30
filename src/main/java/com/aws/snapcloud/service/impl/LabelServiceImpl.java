package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.entity.Label;
import com.aws.snapcloud.repository.LabelRepository;
import com.aws.snapcloud.service.LabelService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;

    public LabelServiceImpl(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    @Override
    public Optional<Label> findByName(String name) {
        return labelRepository.findByName(name);
    }

    @Override
    public Label save(Label label) {
        return labelRepository.save(label);
    }
}
