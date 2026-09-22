package com.rozgarmitra.in.repository;

import com.rozgarmitra.in.Entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    boolean existsByName(String name);

    java.util.List<ServiceCategory> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name,
            String description);
}
