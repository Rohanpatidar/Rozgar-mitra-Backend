package com.rozgarmitra.in.repository;

import com.rozgarmitra.in.Entity.Task;
import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.Entity.ServiceCategory;
import com.rozgarmitra.in.Enum.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(attributePaths = { "customer", "labour", "serviceCategory" })
    List<Task> findByCustomerOrderByCreatedAtDesc(User customer);
    @EntityGraph(attributePaths = { "customer", "labour", "serviceCategory" })
    List<Task> findByLabourOrderByCreatedAtDesc(User labour);
    @EntityGraph(attributePaths = { "customer", "labour", "serviceCategory", "rejectedLabours" })
    List<Task> findDistinctByStatusOrderByCreatedAtDesc(TaskStatus status);

    @EntityGraph(attributePaths = { "customer", "labour", "serviceCategory" })
    List<Task> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = { "customer", "labour", "serviceCategory" })
    Page<Task> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "select t from Task t join fetch t.customer c join fetch t.serviceCategory s left join fetch t.labour l "
            + "where lower(s.name) like lower(concat('%', :query, '%')) "
            + "or lower(s.description) like lower(concat('%', :query, '%')) "
            + "or lower(c.name) like lower(concat('%', :query, '%')) "
            + "or lower(l.name) like lower(concat('%', :query, '%')) "
            + "order by t.createdAt desc", countQuery = "select count(t) from Task t join t.customer c join t.serviceCategory s left join t.labour l "
                    + "where lower(s.name) like lower(concat('%', :query, '%')) "
                    + "or lower(s.description) like lower(concat('%', :query, '%')) "
                    + "or lower(c.name) like lower(concat('%', :query, '%')) "
                    + "or lower(l.name) like lower(concat('%', :query, '%'))")
    Page<Task> searchTasks(@Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = { "customer", "labour", "serviceCategory" })
    Page<Task> findByCustomerOrderByCreatedAtDesc(User customer, Pageable pageable);

    List<Task> findByCustomerAndServiceCategoryAndStatusIn(
            User customer,
            ServiceCategory serviceCategory,
            List<TaskStatus> statuses);

    List<Task> findByStatusAndCreatedAtBefore(TaskStatus status, LocalDateTime cutoff);

    List<Task> findByLabourAndStatusIn(User labour, List<TaskStatus> statuses);

    long countByLabour(User labour);

    @Query("select coalesce(sum(t.finalAmount), 0) from Task t where t.labour = :labour "
            + "and t.status = com.rozgarmitra.in.Enum.TaskStatus.COMPLETED "
            + "and t.completedAt >= :startOfDay")
    double sumTodayEarnings(@Param("labour") User labour, @Param("startOfDay") LocalDateTime startOfDay);

    @EntityGraph(attributePaths = { "customer", "labour", "serviceCategory", "rejectedLabours" })
    List<Task> findByRejectedLaboursContainingOrderByCreatedAtDesc(User labour);
}