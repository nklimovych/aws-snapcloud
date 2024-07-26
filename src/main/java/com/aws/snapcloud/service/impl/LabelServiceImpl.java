package com.aws.snapcloud.service.impl;

import com.aws.snapcloud.service.LabelService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private static final String PREFIX = "label-";
    private static final long INCREMENT = 1L;

    private final S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public Set<String> saveLabels(Set<Label> labels, String key) {
        List<Tag> tags = labels.stream()
                               .map(label -> Tag.builder()
                                                .key(PREFIX + label.name().hashCode())
                                                .value(label.name())
                                                .build())
                               .collect(Collectors.toList());

        s3Client.putObjectTagging(PutObjectTaggingRequest.builder()
                                                         .bucket(bucketName)
                                                         .key(key)
                                                         .tagging(Tagging.builder()
                                                                         .tagSet(tags)
                                                                         .build())
                                                         .build());

        return tags.stream()
                   .map(Tag::value)
                   .collect(Collectors.toSet());
    }

    @Override
    public List<String> findTopLabels(int limit) {
        Map<String, Long> labelCounts = countLabelOccurrences();
        return labelCounts.entrySet().stream()
                          .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                          .limit(limit)
                          .map(Map.Entry::getKey)
                          .collect(Collectors.toList());
    }

    private Map<String, Long> countLabelOccurrences() {
        Map<String, Long> labelCounts = new HashMap<>();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                                                           .bucket(bucketName)
                                                           .build();

        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);
            response.contents().forEach(s3Object -> {
                String key = s3Object.key();
                GetObjectTaggingResponse taggingResponse = s3Client.getObjectTagging(
                        GetObjectTaggingRequest.builder()
                                               .bucket(bucketName)
                                               .key(key)
                                               .build());

                taggingResponse.tagSet().forEach(tag -> {
                    if (tag.key().startsWith(PREFIX)) {
                        labelCounts.merge(tag.value(), INCREMENT, Long::sum);
                    }
                });
            });

            request = request.toBuilder()
                             .continuationToken(response.nextContinuationToken())
                             .build();
        } while (response.isTruncated());

        return labelCounts;
    }
}
