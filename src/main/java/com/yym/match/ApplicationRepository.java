package com.yym.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByPostIdOrderByCreatedAtAsc(Long postId);

    List<Application> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    Optional<Application> findByPostIdAndApplicantId(Long postId, Long applicantId);

    long countByPostIdAndStatus(Long postId, Application.Status status);

    @Transactional
    @Modifying
    @Query("delete from Application a where a.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Query("select p from Post p where p.status = :closed and (p.author.id = :memberId or exists "
            + "(select 1 from Application a where a.post.id = p.id and a.applicant.id = :memberId "
            + "and a.status = :accepted)) order by p.createdAt desc")
    List<Post> findChatRooms(@Param("memberId") Long memberId,
                             @Param("closed") Post.Status closed,
                             @Param("accepted") Application.Status accepted);
}