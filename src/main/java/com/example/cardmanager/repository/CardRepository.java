package com.example.cardmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.cardmanager.model.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserId(Long userId);

    List<Card> findByUserIdAndNameContainingIgnoreCase(Long userId, String keyword);

    @Query("SELECT COALESCE(SUM(CAST(c.price as long)), 0) FROM Card c WHERE c.userId = :userId")
    Long sumPriceByUserId(Long userId);

    long countByUserId(Long userId); 
    List<Card> findTop3ByUserIdOrderByIdDesc(Long userId); 
}
