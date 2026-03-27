package com.brainstorm.brainstorm_api.repository;

import com.brainstorm.brainstorm_api.entity.KeywordLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordLikeRepository extends JpaRepository<KeywordLike, Long> {

    Optional<KeywordLike> findByKeywordIdAndUserId(Long keywordId, UUID userId);

    boolean existsByKeywordIdAndUserId(Long keywordId, UUID userId);

    long countByKeywordId(Long keywordId);
}
