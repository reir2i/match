package com.yym.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByPostIdOrderByIdAsc(Long postId);

    List<ChatMessage> findByPostIdAndIdGreaterThanOrderByIdAsc(Long postId, Long afterId);

    long countByPostId(Long postId);
}