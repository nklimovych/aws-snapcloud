package com.aws.snapcloud.mapper;

import static java.util.stream.Collectors.toSet;

import com.aws.snapcloud.config.MapperConfig;
import com.aws.snapcloud.dto.ImageResponseDto;
import com.aws.snapcloud.entity.Image;
import com.aws.snapcloud.entity.Label;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface ImageMapper {

    @Mapping(source = "labels", target = "labels", qualifiedByName = "labelsToNames")
    ImageResponseDto toResponseDto(Image image);

    @Named("labelsToNames")
    default Set<String> labelsToNames(Set<Label> labels) {
        return labels.stream()
                     .map(Label::getName)
                     .collect(toSet());
    }
}
