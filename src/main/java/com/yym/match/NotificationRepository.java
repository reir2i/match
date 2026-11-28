package com.yym.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    long countByReceiverIdAndCheckedFalse(Long receiverId);

    @Transactional
    @Modifying
    @Query("update Notification n set n.checked = true where n.receiver.id = :receiverId and n.checked = false")
    int markAllChecked(@Param("receiverId") Long receiverId);
}