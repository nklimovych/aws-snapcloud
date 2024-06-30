package com.aws.snapcloud.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Data
public class ImageResponseDto {
    private String name;
    private LocalDateTime uploadedAt;
    private String url;
    private Set<String> labels;
}
