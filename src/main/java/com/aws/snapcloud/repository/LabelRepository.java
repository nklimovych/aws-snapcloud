package com.aws.snapcloud.repository;

import com.aws.snapcloud.entity.Label;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LabelRepository extends JpaRepository<Label, Long> {

    @Query("SELECT l FROM Label l LEFT JOIN FETCH l.images WHERE l.name = :name")
    Optional<Label> findByName(@Param("name") String name);

    @Query("SELECT l.name FROM Label l JOIN l.images i "
            + "GROUP BY l.id, l.name ORDER BY COUNT(i) DESC LIMIT :limit")
    List<String> findTopLabels(@Param("limit") int limit);
}
