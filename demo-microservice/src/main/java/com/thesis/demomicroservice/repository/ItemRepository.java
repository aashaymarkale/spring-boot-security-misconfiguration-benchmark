package com.thesis.demomicroservice.repository;

import com.thesis.demomicroservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    long countByQuantityLessThan(int threshold);
}
