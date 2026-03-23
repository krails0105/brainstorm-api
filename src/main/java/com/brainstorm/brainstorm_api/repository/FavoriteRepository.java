package com.brainstorm.brainstorm_api.repository;

import com.brainstorm.brainstorm_api.entity.Favorite;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Page<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Favorite> findByUserIdAndRoomId(UUID userId, Long roomId);
}
