package com.aws.snapcloud.repository;

import com.aws.snapcloud.entity.Image;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i JOIN i.labels l WHERE l.name = :labelName")
    List<Image> findByLabelName(@Param("labelName") String labelName);

    boolean existsByName(String name);
}
