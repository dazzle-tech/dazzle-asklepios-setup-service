package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.InventoryTransferAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryTransferAttachmentsRepository extends JpaRepository<InventoryTransferAttachments, Long> {

    List<InventoryTransferAttachments> findByTransactionIdAndDeletedAtIsNullOrderByCreatedDateDesc(Long transactionId);

    Optional<InventoryTransferAttachments> findByIdAndDeletedAtIsNull(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update InventoryTransferAttachments i set i.deletedAt = CURRENT_TIMESTAMP where i.id = :id and i.deletedAt is null")
    int softDelete(Long id);
}
