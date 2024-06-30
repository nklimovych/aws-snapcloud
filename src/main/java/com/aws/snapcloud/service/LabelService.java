package com.aws.snapcloud.service;

import com.aws.snapcloud.entity.Label;
import java.util.Optional;

public interface LabelService {
    Optional<Label> findByName(String name);

    Label save(Label label);
}
