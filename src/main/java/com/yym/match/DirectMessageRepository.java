package com.yym.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    List<DirectMessage> findTop100ByReceiverIdAndReceiverDeletedFalseOrderByCreatedAtDesc(Long receiverId);

    List<DirectMessage> findTop100BySenderIdAndSenderDeletedFalseOrderByCreatedAtDesc(Long senderId);

    long countByReceiverIdAndCheckedFalseAndReceiverDeletedFalse(Long receiverId);
}