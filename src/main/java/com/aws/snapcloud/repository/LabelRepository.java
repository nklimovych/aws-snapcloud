package com.aws.snapcloud.repository;

import com.aws.snapcloud.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
}
