package com.brainstorm.brainstorm_api.repository;

import com.brainstorm.brainstorm_api.entity.RoomMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    List<RoomMember> findByRoomId(Long roomId);

    void deleteByRoomIdAndUserId(Long roomID, Long userId);

    long countByRoomId(Long roomId);
}
