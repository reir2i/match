package com.yym.match;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select p from Post p "
            + "order by case when (p.status = :open and p.deadline >= :today) then 0 else 1 end, p.createdAt desc",
            countQuery = "select count(p) from Post p")
    Page<Post> findAllRecruitingFirst(@Param("open") Post.Status open,
                                      @Param("today") LocalDate today,
                                      Pageable pageable);

    @Query(value = "select p from Post p "
            + "where p.status = :open and p.deadline >= :today order by p.createdAt desc",
            countQuery = "select count(p) from Post p where p.status = :open and p.deadline >= :today")
    Page<Post> findRecruiting(@Param("open") Post.Status open,
                              @Param("today") LocalDate today,
                              Pageable pageable);
}