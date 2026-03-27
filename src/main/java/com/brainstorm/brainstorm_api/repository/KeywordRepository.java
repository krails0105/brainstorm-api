package com.brainstorm.brainstorm_api.repository;

import com.brainstorm.brainstorm_api.entity.Keyword;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findByRoomId(Long roomId);

    List<Keyword> findByRoomIdAndUserId(Long roomId, UUID userID);

    long countByRoomIdAndUserId(Long roomId, UUID userId);
}
