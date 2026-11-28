package com.yym.match;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select p from Post p where "
            + "(p.title like concat('%', :keyword, '%') or p.content like concat('%', :keyword, '%')) "
            + "and (:tagId = 0 or exists (select 1 from p.tags t where t.id = :tagId)) "
            + "and (:onlyOpen = false or (p.status = :open and p.deadline >= :today)) "
            + "order by case when (p.status = :open and p.deadline >= :today) then 0 else 1 end, p.createdAt desc",
            countQuery = "select count(p) from Post p where "
                    + "(p.title like concat('%', :keyword, '%') or p.content like concat('%', :keyword, '%')) "
                    + "and (:tagId = 0 or exists (select 1 from p.tags t where t.id = :tagId)) "
                    + "and (:onlyOpen = false or (p.status = :open and p.deadline >= :today))")
    Page<Post> search(@Param("keyword") String keyword,
                      @Param("tagId") long tagId,
                      @Param("onlyOpen") boolean onlyOpen,
                      @Param("open") Post.Status open,
                      @Param("today") LocalDate today,
                      Pageable pageable);
}