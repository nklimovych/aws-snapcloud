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
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
    private final S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public Set<String> saveLabels(Set<Label> labels, String key) {
        List<Tag> tagList = labels.stream()
                                  .map(label -> Tag.builder()
                                                   .key("label-" + label.name().hashCode())
                                                   .value(label.name())
                                                   .build())
                                  .collect(Collectors.toList());

        Tagging tags = Tagging.builder()
                              .tagSet(tagList)
                              .build();

        s3Client.putObjectTagging(PutObjectTaggingRequest.builder()
                                                         .bucket(bucketName)
                                                         .key(key)
                                                         .tagging(tags)
                                                         .build());

        return tagList.stream()
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

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                                                                      .bucket(bucketName)
                                                                      .build();
        ListObjectsV2Response listObjectsResponse;
        do {
            listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

            List<S3Object> s3Objects = listObjectsResponse.contents();
            for (S3Object s3Object : s3Objects) {
                String key = s3Object.key();
                GetObjectTaggingResponse taggingResponse = getObjectTags(key);

                List<Tag> tags = taggingResponse.tagSet();
                for (Tag tag : tags) {
                    if (tag.key().startsWith("label-")) {
                        labelCounts.put(
                                tag.value(),
                                labelCounts.getOrDefault(tag.value(), 0L) + 1
                        );
                    }
                }
            }

            listObjectsRequest = listObjectsRequest.toBuilder()
                                               .continuationToken(
                                                       listObjectsResponse.nextContinuationToken())
                                               .build();
        } while (listObjectsResponse.isTruncated());

        return labelCounts;
    }

    private GetObjectTaggingResponse getObjectTags(String key) {
        return s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                                                                .bucket(bucketName)
                                                                .key(key)
                                                                .build());
    }
}
