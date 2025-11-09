package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.InventoryTransactionAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryTransactionAttachmentsRepository extends JpaRepository<InventoryTransactionAttachments, Long> {

    List<InventoryTransactionAttachments> findByTransactionIdAndDeletedAtIsNullOrderByCreatedDateDesc(Long transactionId);

    Optional<InventoryTransactionAttachments> findByIdAndDeletedAtIsNull(Long id);
}
