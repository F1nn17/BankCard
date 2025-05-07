package com.shiraku.bankcard.repository;

import com.shiraku.bankcard.model.Status;
import com.shiraku.bankcard.model.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    Page<Card> findByOwnerIdAndStatus(UUID ownerId, Status status, Pageable pageable);
    Page<Card> findByOwnerId(UUID ownerId, Pageable pageable);
}
