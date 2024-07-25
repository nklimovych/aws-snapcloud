package com.aws.snapcloud.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageResponseDto {
    private String name;
    private String url;
    private Set<String> tags;
}
